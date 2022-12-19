package ru.mralexeimk.yedom.services;

import lombok.Getter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.FriendsConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.utils.enums.UsersConnectionType;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for working with friends system
 * @author mralexeimk
 */
@Service
public class FriendsService {
    private final LogsService logsService;
    private final FriendsConfig friendsConfig;
    private final UsersRepository usersRepository;

    @Getter
    private AsSynchronizedGraph<Integer, DefaultEdge> followingGraph;
    @Getter
    private AsSynchronizedGraph<Integer, DefaultEdge> friendsGraph;

    @Autowired
    public FriendsService(LogsService logsService, FriendsConfig friendsConfig, UsersRepository usersRepository) {
        this.logsService = logsService;
        this.friendsConfig = friendsConfig;
        this.usersRepository = usersRepository;

        followingGraph = new AsSynchronizedGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));;
        friendsGraph = new AsSynchronizedGraph<>(new DefaultUndirectedGraph<>(DefaultEdge.class));;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        logsService.info("Service started: " + this.getClass().getSimpleName());
        initFromDB();
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(friendsConfig.getSaveToDBPeriodHours() * 60 * 60 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                saveToDB();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        saveToDB();
    }

    /**
     * Init graph from database table 'users'
     */
    public void initFromDB() {
        usersRepository.findAll().forEach(user -> {
            followingGraph.addVertex(user.getId());
            friendsGraph.addVertex(user.getId());
        });
        usersRepository.findAll().forEach(user -> {
            Arrays.stream(user.getFollowingIds().split(",")).forEach(following -> {
                if(following.equals("")) return;
                followingGraph.addEdge(user.getId(), Integer.parseInt(following));
            });
            Arrays.stream(user.getFriendsIds().split(",")).forEach(friend -> {
                if(friend.equals("")) return;
                friendsGraph.addEdge(user.getId(), Integer.parseInt(friend));
            });
        });
    }

    /**
     * Save graph to database table 'users'
     */
    public void saveToDB() {
        logsService.info("Saving 'friends' to database'");
        usersRepository.findAll().forEach(user -> {
            user.setFollowingIds(followingGraph.outgoingEdgesOf(user.getId()).stream()
                    .map(edge -> followingGraph.getEdgeTarget(edge).toString())
                    .reduce((s1, s2) -> s1 + "," + s2).orElse(""));
            user.setFriendsIds(friendsGraph.edgesOf(user.getId()).stream()
                    .map(edge -> friendsGraph.getEdgeTarget(edge).toString())
                    .reduce((s1, s2) -> s1 + "," + s2).orElse(""));
            usersRepository.save(user);
        });
        logsService.info("'friends' saved to database");
    }

    /**
     * Clear graphs
     */
    public void clear() {
        followingGraph = new AsSynchronizedGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));;
        friendsGraph = new AsSynchronizedGraph<>(new DefaultUndirectedGraph<>(DefaultEdge.class));;
    }

    /**
     * Add user in graphs if not exist
     */
    private boolean addIfNotExist(int userId) {
        if(followingGraph.containsVertex(userId)) return true;

        UserEntity user = usersRepository.findById(userId).orElse(null);
        if(user != null) {
            followingGraph.addVertex(userId);
            friendsGraph.addVertex(userId);
            return true;
        }
        return false;
    }

    /**
     * Get connection type between two users (friends/following/follower/strangers)
     */
    public UsersConnectionType getConnectionBetweenUsers(int userId1, int userId2) {
        if(!addIfNotExist(userId1) || !addIfNotExist(userId2)) return UsersConnectionType.STRANGERS;
        if (friendsGraph.containsEdge(userId1, userId2)) {
            return UsersConnectionType.FRIENDS;
        } else if (followingGraph.containsEdge(userId1, userId2)) {
            return UsersConnectionType.FOLLOWING;
        } else if (followingGraph.containsEdge(userId2, userId1)) {
            return UsersConnectionType.FOLLOWER;
        } else {
            return UsersConnectionType.STRANGERS;
        }
    }

    /**
     * Change connection type (follow/unfollow/add to friends/remove from friends)
     * by pressing button on 'userId2' profile page
     * @return operation is successful?
     */
    public boolean changeConnectionTypeBetweenUsers(int userId1, int userId2) {
        if(!addIfNotExist(userId1) || !addIfNotExist(userId2)) return false;
        UsersConnectionType connectionType = getConnectionBetweenUsers(userId1, userId2);
        switch (connectionType) {
            case STRANGERS -> {
                if(followingGraph.outgoingEdgesOf(userId1).size()
                        >= friendsConfig.getMaxConnectedUsers()) return false;
                followingGraph.addEdge(userId1, userId2);
            }
            case FOLLOWING -> followingGraph.removeEdge(userId1, userId2);
            case FOLLOWER -> {
                followingGraph.removeEdge(userId2, userId1);

                if(friendsGraph.edgesOf(userId1).size()
                        >= friendsConfig.getMaxConnectedUsers()) return false;
                friendsGraph.addEdge(userId1, userId2);
            }
            case FRIENDS -> friendsGraph.removeEdge(userId1, userId2);
        }
        return true;
    }

    /**
     * Get list of friends ids of 'userId'
     */
    public List<Integer> getFriendsIdsList(int userId) {
        return friendsGraph.edgesOf(userId).stream()
                .map(edge -> friendsGraph.getEdgeTarget(edge) == userId
                        ? friendsGraph.getEdgeSource(edge)
                        : friendsGraph.getEdgeTarget(edge))
                .collect(Collectors.toList());
    }

    /**
     * Get list of friends of 'userId'
     */
    public List<UserEntity> getFriendsList(int userId) {
        return getFriendsIdsList(userId).stream()
                .map(usersRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get list of following ids of 'userId'
     */
    public List<Integer> getFollowingIdsList(int userId) {
        return followingGraph.outgoingEdgesOf(userId).stream()
                .map(edge -> followingGraph.getEdgeTarget(edge) == userId
                        ? followingGraph.getEdgeSource(edge)
                        : followingGraph.getEdgeTarget(edge))
                .collect(Collectors.toList());
    }

    /**
     * Get list of following of 'userId'
     */
    public List<UserEntity> getFollowingList(int userId) {
        return getFollowingIdsList(userId).stream()
                .map(usersRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get list of followers ids of 'userId'
     */
    public List<Integer> getFollowersIdsList(int userId) {
        return followingGraph.incomingEdgesOf(userId).stream()
                .map(edge -> followingGraph.getEdgeTarget(edge) == userId
                        ? followingGraph.getEdgeSource(edge)
                        : followingGraph.getEdgeTarget(edge))
                .collect(Collectors.toList());
    }

    /**
     * Get list of followers of 'userId'
     */
    public List<UserEntity> getFollowersList(int userId) {
        return getFollowersIdsList(userId).stream()
                .map(usersRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get count of friends of user
     */
    public int getCountOfFriends(int userId) {
        if(!addIfNotExist(userId)) return 0;
        return friendsGraph.edgesOf(userId).size();
    }

    /**
     * Get count of followers of user
     */
    public int getCountOfFollowers(int userId) {
        if(!addIfNotExist(userId)) return 0;
        return followingGraph.incomingEdgesOf(userId).size();
    }

    /**
     * Get count of following of user
     */
    public int getCountOfFollowing(int userId) {
        if(!addIfNotExist(userId)) return 0;
        return followingGraph.outgoingEdgesOf(userId).size();
    }
}
