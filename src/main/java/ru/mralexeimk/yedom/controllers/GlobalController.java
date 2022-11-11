package ru.mralexeimk.yedom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.UtilsService;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

@ControllerAdvice
public class GlobalController {
    private final UtilsService utilsService;
    private final UserRepository userRepository;

    @Autowired
    public GlobalController(UtilsService utilsService, UserRepository userRepository) {
        this.utilsService = utilsService;
        this.userRepository = userRepository;
    }

    /**
     * Global controller for all pages
     * Functions:
     * 1. Update user last activity time with 1 minute interval
     */
    @ModelAttribute
    public void global(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check == null) {
            User user = (User) session.getAttribute("user");
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
