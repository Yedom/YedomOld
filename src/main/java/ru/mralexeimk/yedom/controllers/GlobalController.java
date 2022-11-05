package ru.mralexeimk.yedom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

@ControllerAdvice
public class GlobalController {
    private final UserRepository userRepository;

    @Autowired
    public GlobalController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void myMethod(Model model, HttpSession session) {
        if(session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if(user.isEmailConfirmed()) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Timestamp lastLogin = user.getLastLogin();
                if(now.getTime() - lastLogin.getTime() > 1000*60) {
                    UserEntity userEntity = userRepository.findByEmail(user.getEmail()).orElse(null);
                    if(userEntity != null) {
                        user.setLastLogin(now);
                        userEntity.setLastLogin(now);
                        userRepository.save(userEntity);
                        session.setAttribute("user", user);
                    }
                }
            }
        }
    }
}
