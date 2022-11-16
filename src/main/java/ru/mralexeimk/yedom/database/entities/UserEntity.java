package ru.mralexeimk.yedom.database.entities;

import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.UtilsService;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "role")
    private String role = "user";

    @Column(name = "created_on")
    private Timestamp createdOn;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @Column(name = "balance")
    private int balance = 0;

    @Column(name = "avatar")
    private String avatar = "";

    @Column(name = "courses_ids")
    private String coursesIds = "";

    @Column(name = "draft_courses_ids")
    private String draftCoursesIds = "";

    @Column(name = "friends_ids")
    private String friendsIds = "";

    @Column(name = "following_ids")
    private String followingIds = "";

    @Column(name = "followers_ids")
    private String followersIds = "";

    @Column(name = "organizations_ids")
    private String organizationsIds = "";

    @Column(name = "in_organizations_ids")
    private String inOrganizationsIds = "";

    @Column(name = "organizations_following_ids")
    private String organizationsFollowingIds = "";

    @Column(name = "settings")
    private String settings = "lang=auto&strangersShowEmail=false&strangersShowLinks=false&strangersShowCompletedCourses=false&strangersShowOrganizations=false&friendsShowEmail=false&friendsShowLinks=false&friendsShowCompletedCourses=false&friendsShowOrganizations=true";

    @Column(name = "links")
    private String links = "";

    @Column(name = "about")
    private String about = "";

    // Add to database constructor
    public UserEntity(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.createdOn = user.getCreatedOn();
        this.lastLogin = user.getLastLogin();
        this.avatar = user.getAvatar();
        this.links = user.getLinks();
        this.about = user.getAbout();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity userEntity = (UserEntity) o;
        return Comparator.comparingInt(UserEntity::getId)
                .compare(this, userEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdOn=" + createdOn +
                ", lastLogin=" + lastLogin +
                ", balance=" + balance +
                ", coursesIds='" + coursesIds + '\'' +
                ", draftCoursesIds='" + draftCoursesIds + '\'' +
                ", friendsIds='" + friendsIds + '\'' +
                ", followingIds='" + followingIds + '\'' +
                ", followersIds='" + followersIds + '\'' +
                ", organizationsIds='" + organizationsIds + '\'' +
                ", inOrganizationsIds='" + inOrganizationsIds + '\'' +
                ", organizationsFollowingIds='" + organizationsFollowingIds + '\'' +
                ", settings='" + settings + '\'' +
                ", links='" + links + '\'' +
                '}';
    }
}
