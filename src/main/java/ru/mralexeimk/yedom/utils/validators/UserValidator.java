package ru.mralexeimk.yedom.utils.validators;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.config.configs.AuthConfig;
import ru.mralexeimk.yedom.config.configs.EmailConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

/**
 * User model validator
 */
@Component
public class UserValidator implements Validator {
    private final UtilsService utilsService;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthConfig authConfig;
    private final EmailConfig emailConfig;

    @Autowired
    public UserValidator(UtilsService utilsService, UserRepository userRepository, PasswordEncoder encoder, AuthConfig authConfig, EmailConfig emailConfig) {
        this.utilsService = utilsService;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.authConfig = authConfig;
        this.emailConfig = emailConfig;
    }

    @Override
    public boolean supports(@NonNull Class<?> aClass) {
        return User.class.equals(aClass);
    }

    public void commonValidator(User user, Errors errors) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            utilsService.reject("username", "auth.username.empty", errors);
        }
        else if(user.getUsername().length() < authConfig.getMinUsernameLength() ||
                user.getUsername().length() > authConfig.getMaxUsernameLength()) {
            utilsService.reject("username", "auth.username.size", errors);
        }
        if(user.getPassword() == null || user.getPassword().isEmpty()) {
            utilsService.reject("password", "auth.password.empty", errors);
        }
        else if(user.getPassword().length() < authConfig.getMinPasswordLength()) {
            utilsService.reject("password", "auth.password.size", errors);
        }
        else if(user.getPassword().length() > authConfig.getMaxPasswordLength()) {
            utilsService.reject("password", "auth.password.large", errors);
        }
        if(user.getEmail() == null || user.getEmail().isEmpty()) {
            utilsService.reject("email", "auth.email.empty", errors);
        }
        else if(!utilsService.regexMatch(emailConfig.getRegexp(), user.getEmail())) {
            utilsService.reject("email", "auth.email.incorrect", errors);
        }
    }

    public void regValidator(User user, Errors errors) {
        commonValidator(user, errors);
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            utilsService.reject("email", "auth.email.in_use", errors);
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            utilsService.reject("username", "auth.username.in_use", errors);
        }
    }

    public void loginValidator(User user, Errors errors) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            utilsService.reject("username", "auth.username.empty", errors);
        }
        else if(user.getPassword() == null || user.getPassword().isEmpty()) {
            utilsService.reject("password", "auth.password.empty", errors);
        }
        else {
            if(userRepository.findByUsername(user.getUsername()).isEmpty()) {
                utilsService.reject("username", "auth.user_not_found", errors);
            }
            else {
                UserEntity userEntity = userRepository.findByUsername(user.getUsername()).orElse(null);
                if (userEntity != null && !encoder.matches(user.getPassword(), userEntity.getPassword())) {
                    utilsService.reject("password", "auth.password.incorrect", errors);
                }
            }
        }
    }

    public void updateValidator(User user, Errors errors) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            utilsService.reject("username", "auth.username.empty", errors);
        }
        else if(user.getUsername().length() < authConfig.getMinUsernameLength() ||
                user.getUsername().length() > authConfig.getMaxUsernameLength()) {
            utilsService.reject("username", "auth.username.size", errors);
        }
        else if (userRepository.findByUsername(user.getUsername()).isPresent()
                && userRepository.findByEmail(user.getEmail()).isPresent()
                && userRepository.findByUsername(user.getUsername()).get().getId() !=
                userRepository.findByEmail(user.getEmail()).get().getId()) {
            utilsService.reject("username", "auth.username.in_use", errors);
        }
        if(user.getNewPassword() != null && !user.getNewPassword().equals("")) {
            if (user.getNewPassword().length() < authConfig.getMinPasswordLength()) {
                utilsService.reject("newPassword", "auth.lk.new_password.size", errors);
            }
            else if(user.getNewPassword().length() > authConfig.getMaxPasswordLength()) {
                utilsService.reject("newPassword", "auth.lk.new_password.large", errors);
            }
            else if (user.getNewPasswordRepeat() == null ||
                    !user.getNewPassword().equals(user.getNewPasswordRepeat())) {
                utilsService.reject("newPassword", "auth.lk.new_password.different", errors);
            }
        }
        UserEntity userEntity = userRepository.findByEmail(user.getEmail()).orElse(null);
        if(user.getPassword() == null || user.getPassword().equals("")) {
            utilsService.reject("password", "auth.password.empty", errors);
        }
        else if(user.getPassword().length() < authConfig.getMinPasswordLength()) {
            utilsService.reject("password", "auth.password.size", errors);
        }
        else if(user.getPassword().length() > authConfig.getMaxPasswordLength()) {
            utilsService.reject("password", "auth.password.large", errors);
        }
        else if (userEntity != null && !encoder.matches(user.getPassword(), userEntity.getPassword())) {
            utilsService.reject("password", "auth.password.incorrect", errors);
        }
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        if(o instanceof User user) {
            if(user.getArgs().contains("onReg")) {
                regValidator(user, errors);
            }
            else if(user.getArgs().contains("onLogin")) {
                loginValidator(user, errors);
            }
            else if(user.getArgs().contains("onUpdate")) {
                updateValidator(user, errors);
            }
        }
    }
}
