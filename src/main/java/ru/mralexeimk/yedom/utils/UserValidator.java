package ru.mralexeimk.yedom.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

@Component
public class UserValidator implements Validator {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final LanguageUtil languageUtil;

    @Autowired
    public UserValidator(UserRepository userRepository, PasswordEncoder encoder, LanguageUtil languageUtil) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.languageUtil = languageUtil;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    public void regValidator(User user, Errors errors) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            errors.rejectValue("email", "",
                    languageUtil.getLocalizedMessage("auth.email.in_use"));
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "",
                    languageUtil.getLocalizedMessage("auth.username.in_use"));
        }
    }

    public void updateValidator(User user, Errors errors) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "",
                    languageUtil.getLocalizedMessage("auth.username.in_use"));
        }
    }

    public void loginValidator(User user, Errors errors) {
        boolean error = false;
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            errors.rejectValue("username", "",
                    languageUtil.getLocalizedMessage("auth.username.empty"));
            error = true;
        }
        if(user.getPassword() == null || user.getPassword().isEmpty()) {
            errors.rejectValue("password", "",
                    languageUtil.getLocalizedMessage("auth.password.empty"));
            error = true;
        }

        if(!error) {
            if(userRepository.findByUsername(user.getUsername()).isEmpty()) {
                errors.rejectValue("username", "",
                        languageUtil.getLocalizedMessage("auth.user_not_found"));
            }
            else {
                UserEntity userEntity = userRepository.findByUsername(user.getUsername()).orElse(null);
                if (userEntity != null && !encoder.matches(user.getPassword(), userEntity.getPassword())) {
                    errors.rejectValue("password", "",
                            languageUtil.getLocalizedMessage("auth.password_incorrect"));
                }
            }
        }
    }

    @Override
    public void validate(Object o, Errors errors) {
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
