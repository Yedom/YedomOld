package ru.mralexeimk.yedom.models;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.validation.*;
import ru.mralexeimk.yedom.utils.services.RolesService;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.*;

@Data
@AllArgsConstructor
@Component
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

    private boolean emailConfirmed = false;

    private String role = "user";

    private String newPassword = "";

    private String newPasswordRepeat = "";

    private Set<String> args = new HashSet<>();
    private Map<String, String> vals = new HashMap<>();

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = "user";
    }

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public User(UserEntity userEntity) {
        this(userEntity.getUsername(), userEntity.getPassword(), userEntity.getEmail(), userEntity.getRole());
        emailConfirmed = true;
    }

    public User(User cp) {
        this(cp.getUsername(), cp.getPassword(), cp.getEmail(), true, cp.getRole(),
                cp.getNewPassword(), cp.getNewPasswordRepeat(), cp.getArgs(), cp.getVals());
    }

    public User() {

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
