package ru.mralexeimk.yedom.database.entities;

import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role = "user";

    @Column(name = "created_on")
    private Timestamp createdOn = CommonUtils.getCurrentTimestamp();

    @Column(name = "last_login")
    private Timestamp lastLogin = CommonUtils.getCurrentTimestamp();

    @Column(name = "balance")
    private int balance = 0;

    @Column(name = "completed_courses_ids")
    private String completedCoursesIds = "";


    @Column(name = "current_courses_ids")
    private String currentCoursesIds = "";


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

    // Add to database constructor
    public UserEntity(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
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
                ", createOn=" + createdOn +
                ", lastLogin=" + lastLogin +
                ", balance=" + balance +
                ", completedCoursesIds='" + completedCoursesIds + '\'' +
                ", currentCoursesIds='" + currentCoursesIds + '\'' +
                ", coursesIds='" + coursesIds + '\'' +
                ", draftCoursesIds='" + draftCoursesIds + '\'' +
                ", friendsIds='" + friendsIds + '\'' +
                ", followingIds='" + followingIds + '\'' +
                ", followersIds='" + followersIds + '\'' +
                '}';
    }
}
