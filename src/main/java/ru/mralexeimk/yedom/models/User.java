package ru.mralexeimk.yedom.models;

import lombok.*;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.validation.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class User {
    @NotEmpty(message = "{auth.username.empty}", groups = FirstOrder.class)
    @Size(min = YedomConfig.minUsernameLength, max = YedomConfig.maxUsernameLength, message = "{auth.username.size}", groups = SecondOrder.class)
    private String username;

    @NotEmpty(message = "{auth.password.empty}", groups = ThirdOrder.class)
    @Size(min = YedomConfig.minPasswordLength, message = "{auth.password.size}", groups = FourthOrder.class)
    private String password;

    @NotEmpty(message = "{auth.email.empty}", groups = FifthOrder.class)
    @Email(message = "{auth.email.incorrect}", groups = SixthOrder.class)
    private String email;

    private Set<String> args = new HashSet<>();

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(UserEntity userEntity) {
        this(userEntity.getUsername(), userEntity.getPassword(), userEntity.getEmail());
    }

    public User() {

    }

    public User clearArgs() {
        this.args.clear();
        return this;
    }

    public User addArg(String arg) {
        this.args.add(arg);
        return this;
    }

    public User removeArg(String arg) {
        try {
            this.args.remove(arg);
        } catch(Exception ignored) {}
        return this;
    }
}
