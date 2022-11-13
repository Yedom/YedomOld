package ru.mralexeimk.yedom.utils.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.FriendsServerConfig;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.enums.SocketType;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.util.Arrays;
import java.util.List;

@Service
public class FriendsService extends AbstractService {
    private final LanguageUtil languageUtil;
    public final Pair<String, String> DEFAULT_FOLLOW_BTN;
    public final Pair<String, String> DISABLED_FOLLOW_BTN;

    @Autowired
    public FriendsService(FriendsServerConfig friendsServerConfig, LanguageUtil languageUtil) {
        super(friendsServerConfig);
        this.languageUtil = languageUtil;
        DEFAULT_FOLLOW_BTN = new Pair<>(
                languageUtil.getLocalizedMessage("profile.follow"),
                "btn-green");
        DISABLED_FOLLOW_BTN = new Pair<>(
                languageUtil.getLocalizedMessage("profile.following"),
                "btn-disabled");
    }

    public int getFriendsCount(int id) {
        return Integer.parseInt(sendSocket("0", SocketType.FRIENDS_COUNT, String.valueOf(id)));
    }

    public int getFollowingsCount(int id) {
        return Integer.parseInt(sendSocket("0", SocketType.FOLLOWINGS_COUNT, String.valueOf(id)));
    }

    public int getFollowersCount(int id) {
        return Integer.parseInt(sendSocket("0", SocketType.FOLLOWERS_COUNT, String.valueOf(id)));
    }

    public List<Integer> getFriendsList(int id) {
        return Arrays.stream(sendSocket("0", SocketType.FRIENDS_LIST, String.valueOf(id)).split(","))
                .map(Integer::parseInt)
                .toList();
    }

    public List<Integer> getFollowingsList(int id) {
        return Arrays.stream(sendSocket("0", SocketType.FOLLOWINGS_LIST, String.valueOf(id)).split(","))
                .map(Integer::parseInt)
                .toList();
    }

    public List<Integer> getFollowersList(int id) {
        return Arrays.stream(sendSocket("0", SocketType.FOLLOWERS_LIST, String.valueOf(id)).split(","))
                .map(Integer::parseInt)
                .toList();
    }

    private Pair<String, String> connectionToButton(String connection) {
        return switch (connection) {
            case "strangers" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.follow"),
                    "btn-green");
            case "friends" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.friends.delete"),
                    "btn-red");
            case "following" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.unfollow"),
                    "btn-red");
            case "follower" -> new Pair<>(
                    languageUtil.getLocalizedMessage("profile.friends.add"),
                    "btn-green");
            default -> DEFAULT_FOLLOW_BTN;
        };
    }

    public Pair<String, String> getFollowButtonType(int id1, int id2) {
        try {
            return connectionToButton(
                    sendSocket(String.valueOf(id1),
                            SocketType.CONNECTION_TYPE,
                            String.valueOf(id2)));
        } catch (Exception e) {
            return DISABLED_FOLLOW_BTN;
        }
    }

    public void followPress(int id1, int id2) {
        try {
            sendSocket(String.valueOf(id1),
                    SocketType.FOLLOW_PRESS,
                    String.valueOf(id2));
        } catch (Exception ignored) {}
    }
}
