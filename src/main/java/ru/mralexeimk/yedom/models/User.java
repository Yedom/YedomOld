package ru.mralexeimk.yedom.models;

import lombok.*;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.enums.UserValidationType;

import java.sql.Timestamp;
import java.util.*;

/**
 * Model of user (UserEntity + additional model fields)
 */
@Data
@AllArgsConstructor
@Component
@NoArgsConstructor
public class User {
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
    private String organizationsIds = "";
    private String inOrganizationsIds = "";
    private String organizationsFollowingIds = "";
    private UserSettings settings = new UserSettings();
    private String links = "";
    private String about = "";

    //additional fields
    private UserValidationType validationType = UserValidationType.NONE;
    private boolean emailConfirmed = false;

    private String newPassword = "";

    private String newPasswordRepeat = "";

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // User by UserEntity constructor
    public User(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.username = userEntity.getUsername();
        this.password = userEntity.getPassword();
        this.email = userEntity.getEmail();
        this.role = userEntity.getRole();
        this.createdOn = userEntity.getCreatedOn();
        this.lastLogin = userEntity.getLastLogin();
        this.balance = userEntity.getBalance();
        this.avatar = userEntity.getAvatar();
        this.coursesIds = userEntity.getCoursesIds();
        this.draftCoursesIds = userEntity.getDraftCoursesIds();
        this.friendsIds = userEntity.getFriendsIds();
        this.followingIds = userEntity.getFollowingIds();
        this.organizationsIds = userEntity.getOrganizationsIds();
        this.inOrganizationsIds = userEntity.getInOrganizationsIds();
        this.organizationsFollowingIds = userEntity.getOrganizationsFollowingIds();
        this.settings = new UserSettings(userEntity.getSettings());
        this.links = userEntity.getLinks();
        this.about = userEntity.getAbout();
        this.emailConfirmed = true;
    }

    public User(User cp) {
        this(cp.getId(), cp.getUsername(), cp.getPassword(), cp.getEmail(), cp.getRole(), cp.getCreatedOn(),
                cp.getLastLogin(), cp.getBalance(), cp.getAvatar(), cp.getCoursesIds(), cp.getDraftCoursesIds(),
                cp.getFriendsIds(), cp.getFollowingIds(), cp.getOrganizationsIds(), cp.getInOrganizationsIds(),
                cp.getOrganizationsFollowingIds(), cp.getSettings(), cp.getLinks(), cp.getAbout(),
                cp.getValidationType(), cp.isEmailConfirmed(), cp.getNewPassword(), cp.getNewPasswordRepeat());
    }

    public User checkFor(UserValidationType type) {
        this.validationType = type;
        return this;
    }
}
