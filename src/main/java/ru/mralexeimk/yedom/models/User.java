package ru.mralexeimk.yedom.models;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.validation.*;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.services.RolesService;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

@Data
@AllArgsConstructor
@Component
@NoArgsConstructor
public class User {

    // UserEntity fields
    private int id;
    @NotEmpty(message = "{auth.username.empty}", groups = FirstOrder.class)
    @Size(min = YedomConfig.minUsernameLength, max = YedomConfig.maxUsernameLength, message = "{auth.username.size}", groups = SecondOrder.class)
    private String username;

    @NotEmpty(message = "{auth.password.empty}", groups = ThirdOrder.class)
    @Size(min = YedomConfig.minPasswordLength, message = "{auth.password.size}", groups = FourthOrder.class)
    private String password;

    @NotEmpty(message = "{auth.email.empty}", groups = FifthOrder.class)
    @Email(message = "{auth.email.incorrect}", groups = SixthOrder.class)
    private String email;

    private String role = "user";
    private Timestamp createdOn = CommonUtils.getCurrentTimestamp();
    private Timestamp lastLogin = CommonUtils.getCurrentTimestamp();
    private int balance = 0;
    private String completedCoursesIds = "";
    private String currentCoursesIds = "";
    private String coursesIds = "";
    private String draftCoursesIds = "";
    private String friendsIds = "";
    private String followingIds = "";
    private String followersIds = "";
    private String organizationsIds = "";
    private String inOrganizationsIds = "";

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
                Timestamp createdOn, Timestamp lastLogin, int balance,
                String completedCoursesIds, String currentCoursesIds, String coursesIds,
                String draftCoursesIds, String friendsIds, String followingIds, String followersIds,
                String organizationsIds, String inOrganizationsIds) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createdOn = createdOn;
        this.lastLogin = lastLogin;
        this.balance = balance;
        this.completedCoursesIds = completedCoursesIds;
        this.currentCoursesIds = currentCoursesIds;
        this.coursesIds = coursesIds;
        this.draftCoursesIds = draftCoursesIds;
        this.friendsIds = friendsIds;
        this.followingIds = followingIds;
        this.followersIds = followersIds;
        this.organizationsIds = organizationsIds;
        this.inOrganizationsIds = inOrganizationsIds;
    }

    // User by UserEntity constructor
    public User(UserEntity userEntity) {
        this(userEntity.getId(), userEntity.getUsername(), userEntity.getPassword(), userEntity.getEmail(), userEntity.getRole(),
                userEntity.getCreatedOn(), userEntity.getLastLogin(), userEntity.getBalance(),
                userEntity.getCompletedCoursesIds(), userEntity.getCurrentCoursesIds(), userEntity.getCoursesIds(),
                userEntity.getDraftCoursesIds(), userEntity.getFriendsIds(), userEntity.getFollowingIds(), userEntity.getFollowersIds(),
                userEntity.getOrganizationsIds(), userEntity.getInOrganizationsIds());
        emailConfirmed = true;
    }

    public User(User cp) {
        this(cp.getId(), cp.getUsername(), cp.getPassword(), cp.getEmail(), cp.getRole(),
                cp.getCreatedOn(), cp.getLastLogin(), cp.getBalance(),
                cp.getCompletedCoursesIds(), cp.getCurrentCoursesIds(), cp.getCoursesIds(),
                cp.getDraftCoursesIds(), cp.getFriendsIds(), cp.getFollowingIds(), cp.getFollowersIds(),
                cp.getOrganizationsIds(), cp.getInOrganizationsIds(),
                cp.isEmailConfirmed(), cp.getNewPassword(), cp.getNewPasswordRepeat(), cp.getArgs(), cp.getVals());
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
