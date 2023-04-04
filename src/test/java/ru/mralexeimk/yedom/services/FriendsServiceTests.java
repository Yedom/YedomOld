package ru.mralexeimk.yedom.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import ru.mralexeimk.yedom.services.FriendsService;
import ru.mralexeimk.yedom.utils.enums.UsersConnectionType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FriendsServiceTests {
    private final FriendsService friendsService;

    @Autowired
    @Lazy
    public FriendsServiceTests(FriendsService friendsService) {
        this.friendsService = friendsService;
    }

    void addAllVertex(List<Integer> vertexList) {
        for (Integer vertex : vertexList) {
            friendsService.getFriendsGraph().addVertex(vertex);
            friendsService.getFollowingGraph().addVertex(vertex);
        }
    }

    void initGraphs() {
        friendsService.clear();
        addAllVertex(List.of(1, 2, 3, 4, 5, 6));
        friendsService.getFriendsGraph().addEdge(1, 2);
        friendsService.getFriendsGraph().addEdge(1, 3);
        friendsService.getFriendsGraph().addEdge(4, 2);

        friendsService.getFollowingGraph().addEdge(2, 3);
        friendsService.getFollowingGraph().addEdge(5, 1);
    }

    @Test
    void testGraphs() {
        initGraphs();
        assertEquals(friendsService.getCountOfFriends(1), 2);
        assertEquals(friendsService.getCountOfFriends(2), 2);
        assertEquals(friendsService.getCountOfFriends(4), 1);
        assertEquals(friendsService.getFriendsIdsList(1), List.of(2, 3));
        assertEquals(friendsService.getFriendsIdsList(2), List.of(1, 4));
        assertEquals(friendsService.getCountOfFollowing(1), 0);
        assertEquals(friendsService.getCountOfFollowing(2), 1);
        assertEquals(friendsService.getCountOfFollowing(5), 1);
        assertEquals(friendsService.getFollowingIdsList(1), List.of());
        assertEquals(friendsService.getFollowingIdsList(2), List.of(3));
        assertEquals(friendsService.getFollowingIdsList(5), List.of(1));
        assertEquals(friendsService.getCountOfFollowers(2), 0);
        assertEquals(friendsService.getCountOfFollowers(3), 1);
        assertEquals(friendsService.getCountOfFollowers(1), 1);
        assertEquals(friendsService.getFollowersIdsList(2), List.of());
        assertEquals(friendsService.getFollowersIdsList(3), List.of(2));
        assertEquals(friendsService.getFollowersIdsList(1), List.of(5));
    }

    @Test
    void getConnectionBetweenUsers() {
        initGraphs();
        assertEquals(friendsService.getConnectionBetweenUsers(1, 2), UsersConnectionType.FRIENDS);
        assertEquals(friendsService.getConnectionBetweenUsers(1, 3), UsersConnectionType.FRIENDS);
        assertEquals(friendsService.getConnectionBetweenUsers(4, 2), UsersConnectionType.FRIENDS);
        assertEquals(friendsService.getConnectionBetweenUsers(2, 3), UsersConnectionType.FOLLOWING);
        assertEquals(friendsService.getConnectionBetweenUsers(3, 2), UsersConnectionType.FOLLOWER);
        assertEquals(friendsService.getConnectionBetweenUsers(5, 1), UsersConnectionType.FOLLOWING);
        assertEquals(friendsService.getConnectionBetweenUsers(1, 5), UsersConnectionType.FOLLOWER);
        assertEquals(friendsService.getConnectionBetweenUsers(1, 4), UsersConnectionType.STRANGERS);
        assertEquals(friendsService.getConnectionBetweenUsers(10, 12), UsersConnectionType.STRANGERS);
    }

    @Test
    void changeConnectionTypeBetweenUsers() {
        initGraphs();
        assertTrue(friendsService.changeConnectionTypeBetweenUsers(1, 2));
        assertEquals(friendsService.getConnectionBetweenUsers(1, 2), UsersConnectionType.STRANGERS);

        assertTrue(friendsService.changeConnectionTypeBetweenUsers(2, 3));
        assertEquals(friendsService.getConnectionBetweenUsers(2, 3), UsersConnectionType.STRANGERS);

        assertTrue(friendsService.changeConnectionTypeBetweenUsers(1, 5));
        assertEquals(friendsService.getConnectionBetweenUsers(1, 5), UsersConnectionType.FRIENDS);

        assertTrue(friendsService.changeConnectionTypeBetweenUsers(1, 6));
        assertEquals(friendsService.getConnectionBetweenUsers(1, 6), UsersConnectionType.FOLLOWING);

        assertTrue(friendsService.changeConnectionTypeBetweenUsers(6, 1));
        assertEquals(friendsService.getConnectionBetweenUsers(6, 1), UsersConnectionType.FRIENDS);
    }
}
