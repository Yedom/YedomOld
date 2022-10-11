package ru.mralexeimk.yedom.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.database.UserDB;
import ru.mralexeimk.yedom.models.User;

@Component
public class UserValidator implements Validator {
    private final UserDB userDB;
    private final PasswordEncoder encoder;
    private final LanguageUtil languageUtil;

    @Autowired
    public UserValidator(UserDB userDB, PasswordEncoder encoder, LanguageUtil languageUtil) {
        this.userDB = userDB;
        this.encoder = encoder;
        this.languageUtil = languageUtil;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    public void regValidator(User user, Errors errors) {
        if (userDB.getUserByEmail(user.getEmail()) != null) {
            errors.rejectValue("email", "",
                    languageUtil.getLocalizedMessage("auth.email.in_use"));
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
            if(userDB.getUserByUsername(user.getUsername()) == null) {
                errors.rejectValue("username", "",
                        languageUtil.getLocalizedMessage("auth.user_not_found"));
            }
            else {
                User db_user = userDB.getUserByUsername(user.getUsername());
                if (!encoder.matches(user.getPassword(), db_user.getPassword())) {
                    languageUtil.getLocalizedMessage("auth.password_incorrect");
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
        }
    }
}
