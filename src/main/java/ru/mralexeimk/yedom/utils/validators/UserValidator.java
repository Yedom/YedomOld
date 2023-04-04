package ru.mralexeimk.yedom.utils.validators;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.configs.properties.AuthConfig;
import ru.mralexeimk.yedom.configs.properties.EmailConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.enums.UserValidationType;
import ru.mralexeimk.yedom.services.ValidationService;

/**
 * User model validator
 */
@Component
public class UserValidator implements Validator {
    private final ValidationService validationService;
    private final UsersRepository usersRepository;
    private final PasswordEncoder encoder;
    private final AuthConfig authConfig;
    private final EmailConfig emailConfig;

    @Autowired
    public UserValidator(ValidationService validationService, UsersRepository usersRepository, PasswordEncoder encoder, AuthConfig authConfig, EmailConfig emailConfig) {
        this.validationService = validationService;
        this.usersRepository = usersRepository;
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
            validationService.reject("username", "auth.username.empty", errors);
        }
        else if(user.getUsername().length() < authConfig.getMinUsernameLength() ||
                user.getUsername().length() > authConfig.getMaxUsernameLength()) {
            validationService.reject("username", "auth.username.size", errors);
        }
        if(user.getPassword() == null || user.getPassword().isEmpty()) {
            validationService.reject("password", "auth.password.empty", errors);
        }
        else if(user.getPassword().length() < authConfig.getMinPasswordLength()) {
            validationService.reject("password", "auth.password.size", errors);
        }
        else if(user.getPassword().length() > authConfig.getMaxPasswordLength()) {
            validationService.reject("password", "auth.password.large", errors);
        }
        if(user.getEmail() == null || user.getEmail().isEmpty()) {
            validationService.reject("email", "auth.email.empty", errors);
        }
        else if(!validationService.regexMatch(emailConfig.getRegexp(), user.getEmail())) {
            validationService.reject("email", "auth.email.incorrect", errors);
        }
    }

    public void regValidator(User user, Errors errors) {
        commonValidator(user, errors);
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            validationService.reject("email", "auth.email.in_use", errors);
        }
        if (usersRepository.findByUsername(user.getUsername()).isPresent()) {
            validationService.reject("username", "auth.username.in_use", errors);
        }
    }

    public void loginValidator(User user, Errors errors) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            validationService.reject("username", "auth.username.empty", errors);
        }
        else if(user.getPassword() == null || user.getPassword().isEmpty()) {
            validationService.reject("password", "auth.password.empty", errors);
        }
        else {
            if(usersRepository.findByUsername(user.getUsername()).isEmpty()) {
                validationService.reject("username", "auth.user_not_found", errors);
            }
            else {
                UserEntity userEntity = usersRepository.findByUsername(user.getUsername()).orElse(null);
                if (userEntity != null && !encoder.matches(user.getPassword(), userEntity.getPassword())) {
                    validationService.reject("password", "auth.password.incorrect", errors);
                }
            }
        }
    }

    public void updateValidator(User user, Errors errors) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            validationService.reject("username", "auth.username.empty", errors);
        }
        else if(user.getUsername().length() < authConfig.getMinUsernameLength() ||
                user.getUsername().length() > authConfig.getMaxUsernameLength()) {
            validationService.reject("username", "auth.username.size", errors);
        }
        else if (usersRepository.findByUsername(user.getUsername()).isPresent()
                && usersRepository.findByEmail(user.getEmail()).isPresent()
                && usersRepository.findByUsername(user.getUsername()).get().getId() !=
                usersRepository.findByEmail(user.getEmail()).get().getId()) {
            validationService.reject("username", "auth.username.in_use", errors);
        }
        if(user.getNewPassword() != null && !user.getNewPassword().equals("")) {
            if (user.getNewPassword().length() < authConfig.getMinPasswordLength()) {
                validationService.reject("newPassword", "auth.lk.new_password.size", errors);
            }
            else if(user.getNewPassword().length() > authConfig.getMaxPasswordLength()) {
                validationService.reject("newPassword", "auth.lk.new_password.large", errors);
            }
            else if (user.getNewPasswordRepeat() == null ||
                    !user.getNewPassword().equals(user.getNewPasswordRepeat())) {
                validationService.reject("newPassword", "auth.lk.new_password.different", errors);
            }
        }
        UserEntity userEntity = usersRepository.findByEmail(user.getEmail()).orElse(null);
        if(user.getPassword() == null || user.getPassword().equals("")) {
            validationService.reject("password", "auth.password.empty", errors);
        }
        else if(user.getPassword().length() < authConfig.getMinPasswordLength()) {
            validationService.reject("password", "auth.password.size", errors);
        }
        else if(user.getPassword().length() > authConfig.getMaxPasswordLength()) {
            validationService.reject("password", "auth.password.large", errors);
        }
        else if (userEntity != null && !encoder.matches(user.getPassword(), userEntity.getPassword())) {
            validationService.reject("password", "auth.password.incorrect", errors);
        }
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        if(o instanceof User user) {
            if(user.getValidationType() == UserValidationType.REG) {
                regValidator(user, errors);
            }
            else if(user.getValidationType() == UserValidationType.LOGIN) {
                loginValidator(user, errors);
            }
            else if(user.getValidationType() == UserValidationType.UPDATE) {
                updateValidator(user, errors);
            }
        }
    }
}
