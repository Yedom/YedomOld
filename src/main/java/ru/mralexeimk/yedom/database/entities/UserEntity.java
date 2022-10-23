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

    @Column(name = "created_on")
    private Timestamp createOn;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    public UserEntity(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.createOn = CommonUtils.getCurrentTimestamp();
        this.lastLogin = CommonUtils.getCurrentTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity userEntity = (UserEntity) o;
        return Comparator.comparingInt(UserEntity::getId)
                .thenComparing(UserEntity::getUsername)
                .thenComparing(UserEntity::getPassword)
                .thenComparing(UserEntity::getEmail)
                .thenComparing(UserEntity::getCreateOn)
                .thenComparing(UserEntity::getLastLogin)
                .compare(this, userEntity) == 0;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User [id: "+id+", username: "+username+
                ", email: "+email+", createdOn: "+createOn+
                ", lastLogin: "+lastLogin+"]";
    }
}
