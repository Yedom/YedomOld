package ru.mralexeimk.yedom.models;

import lombok.*;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.utils.custom.Pair;

import java.sql.Timestamp;
import java.util.*;

@Data
@AllArgsConstructor
@Component
@NoArgsConstructor
public class User {
    // UserEntity fields
    private int id;
    private String username;
    private String password;
    private String email;

    private String role = "user";
    private Timestamp createdOn;
    private Timestamp lastLogin;
    private int balance = 0;

    private String avatar = "";
    private String coursesIds = "";
    private String draftCoursesIds = "";
    private String friendsIds = "";
    private String followingIds = "";
    private String followersIds = "";
    private String organizationsIds = "";
    private String inOrganizationsIds = "";
    private String organizationsFollowingIds = "";
    private UserSettings settings = new UserSettings();
    private String links = "";
    private String about = "";

    // Model fields
    private boolean emailConfirmed = false;

    private String newPassword = "";

    private String newPasswordRepeat = "";

    private Set<String> args = new HashSet<>();
    private Map<String, String> vals = new HashMap<>();

    // Model registration constructor
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Model all fields constructor
    public User(int id, String username, String password, String email, String role,
                Timestamp createdOn, Timestamp lastLogin, int balance, String avatar,
                String coursesIds, String draftCoursesIds, String friendsIds,
                String followingIds, String followersIds, String organizationsIds,
                String inOrganizationsIds, String organizationsFollowingIds,
                UserSettings settings, String links, String about) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createdOn = createdOn;
        this.lastLogin = lastLogin;
        this.balance = balance;
        this.avatar = avatar;
        this.coursesIds = coursesIds;
        this.draftCoursesIds = draftCoursesIds;
        this.friendsIds = friendsIds;
        this.followingIds = followingIds;
        this.followersIds = followersIds;
        this.organizationsIds = organizationsIds;
        this.inOrganizationsIds = inOrganizationsIds;
        this.organizationsFollowingIds = organizationsFollowingIds;
        this.settings = settings;
        this.links = links;
        this.about = about;
    }

    // User by UserEntity constructor
    public User(UserEntity userEntity) {
        this(userEntity.getId(), userEntity.getUsername(), userEntity.getPassword(),
                userEntity.getEmail(), userEntity.getRole(), userEntity.getCreatedOn(),
                userEntity.getLastLogin(), userEntity.getBalance(), userEntity.getAvatar(),
                userEntity.getCoursesIds(), userEntity.getDraftCoursesIds(), userEntity.getFriendsIds(),
                userEntity.getFollowingIds(), userEntity.getFollowersIds(), userEntity.getOrganizationsIds(),
                userEntity.getInOrganizationsIds(), userEntity.getOrganizationsFollowingIds(),
                new UserSettings(userEntity.getSettings()), userEntity.getLinks(), userEntity.getAbout());
        emailConfirmed = true;
    }

    public User(User cp) {
        this(cp.getId(), cp.getUsername(), cp.getPassword(), cp.getEmail(), cp.getRole(),
                cp.getCreatedOn(), cp.getLastLogin(), cp.getBalance(), cp.getAvatar(),
                cp.getCoursesIds(), cp.getDraftCoursesIds(), cp.getFriendsIds(),
                cp.getFollowingIds(), cp.getFollowersIds(), cp.getOrganizationsIds(),
                cp.getInOrganizationsIds(), cp.getOrganizationsFollowingIds(),
                cp.getSettings(), cp.getLinks(), cp.getAbout(),
                cp.isEmailConfirmed(), cp.getNewPassword(),
                cp.getNewPasswordRepeat(), cp.getArgs(), cp.getVals());
    }

    public User withArgs(String... args) {
        User userClone = new User(this);
        userClone.args.addAll(List.of(args));
        return userClone;
    }

    public User withVal(String key, String value) {
        User userClone = new User(this);
        userClone.vals.put(key, value);
        return userClone;
    }
}
