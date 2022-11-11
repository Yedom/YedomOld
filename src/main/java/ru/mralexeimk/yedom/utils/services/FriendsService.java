package ru.mralexeimk.yedom.utils.services;

import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.util.Arrays;
import java.util.List;

@Service
public class FriendsService {
    private final UtilsService utilsService;
    private final LanguageUtil languageUtil;
    private final UserRepository userRepository;

    public final Pair<String, String> DEFAULT_FOLLOW_BTN;

    public FriendsService(UtilsService utilsService, LanguageUtil languageUtil, UserRepository userRepository) {
        this.utilsService = utilsService;
        this.languageUtil = languageUtil;
        this.userRepository = userRepository;
        DEFAULT_FOLLOW_BTN = new Pair<>(
                languageUtil.getLocalizedMessage("profile.follow"), "btn-green");
    }

    /**
     * Get button action by user's relationships
     */
    public Pair<String, String> getFollowButtonAction(UserEntity userEntity, UserEntity userEntity2) {
        String id = String.valueOf(userEntity2.getId());

        String action = languageUtil.getLocalizedMessage("profile.follow");
        String color = "btn-green";
        try {
            if (Arrays.stream(userEntity.getFriendsIds().split(",")).toList().contains(id)) {
                action = languageUtil.getLocalizedMessage("profile.friends.delete");
                color = "btn-red";
            }
            else if (Arrays.stream(userEntity.getFollowersIds().split(",")).toList().contains(id))
                action = languageUtil.getLocalizedMessage("profile.friends.add");
            else if (Arrays.stream(userEntity.getFollowingIds().split(",")).toList().contains(id)) {
                action = languageUtil.getLocalizedMessage("profile.unfollow");
                color = "btn-red";
            }
        } catch (Exception ignored) {}
        return new Pair<>(action, color);
    }

    public Pair<String, String> getFollowButtonAction(User user, UserEntity userEntity2) {
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        if (userEntity == null) return new Pair<>("", "");

        return getFollowButtonAction(userEntity, userEntity2);
    }

    public int getFriendsCount(UserEntity userEntity) {
        return utilsService.splitToListString(userEntity.getFriendsIds()).size();
    }

    public int getFollowersCount(UserEntity userEntity) {
        return utilsService.splitToListString(userEntity.getFollowersIds()).size();
    }

    public int getFollowingCount(UserEntity userEntity) {
        return utilsService.splitToListString(userEntity.getFollowingIds()).size();
    }

    /**
     * Change user's relationships by 'userEntity' button press
     */
    public void followButtonPress(UserEntity userEntity, UserEntity userEntity2) {
        String id = String.valueOf(userEntity.getId()),
                id2 = String.valueOf(userEntity2.getId());

        List<String> friendsIds = utilsService.splitToListString(userEntity.getFriendsIds()),
                followersIds = utilsService.splitToListString(userEntity.getFollowersIds()),
                followingIds = utilsService.splitToListString(userEntity.getFollowingIds()),
                friendsIds2 = utilsService.splitToListString(userEntity2.getFriendsIds()),
                followersIds2 = utilsService.splitToListString(userEntity2.getFollowersIds()),
                followingIds2 = utilsService.splitToListString(userEntity2.getFollowingIds());

        if(friendsIds.contains(id2)) {
            friendsIds.remove(id2);
            friendsIds2.remove(id);

            userEntity.setFriendsIds(String.join(",", friendsIds));
            userEntity2.setFriendsIds(String.join(",", friendsIds2));
        }
        else if(followersIds.contains(id2)) {
            friendsIds.add(id2);
            friendsIds2.add(id);
            followersIds.remove(id2);
            followingIds2.remove(id);

            userEntity.setFriendsIds(String.join(",", friendsIds));
            userEntity2.setFriendsIds(String.join(",", friendsIds2));
            userEntity.setFollowersIds(String.join(",", followersIds));
            userEntity2.setFollowingIds(String.join(",", followingIds2));
        }
        else if(followingIds.contains(id2)) {
            followingIds.remove(id2);
            followersIds2.remove(id);

            userEntity.setFollowingIds(String.join(",", followingIds));
            userEntity2.setFollowersIds(String.join(",", followersIds2));
        }
        else {
            followingIds.add(id2);
            followersIds2.add(id);

            userEntity.setFollowingIds(String.join(",", followingIds));
            userEntity2.setFollowersIds(String.join(",", followersIds2));
        }

        userRepository.save(userEntity);
        userRepository.save(userEntity2);
    }

    public void followButtonPress(User user, UserEntity userEntity2) {
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        if (userEntity == null) return;

        followButtonPress(userEntity, userEntity2);
    }
}
