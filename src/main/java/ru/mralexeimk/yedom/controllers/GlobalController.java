package ru.mralexeimk.yedom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.services.UtilsService;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;

@ControllerAdvice
public class GlobalController {
    private final UtilsService utilsService;
    private final UsersRepository usersRepository;

    @Autowired
    public GlobalController(UtilsService utilsService, UsersRepository usersRepository) {
        this.utilsService = utilsService;
        this.usersRepository = usersRepository;
    }

    /**
     * Global controller for all pages
     * Functions:
     * 1. Add auth attribute to model
     * 2. Update user last activity time with 1 minute interval
     * 3. Attempt to create socket connection between user and server if it is not exist
     */
    @ModelAttribute
    public void global(Model model, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        boolean auth = false;
        String role = "user";

        if(check == null) {
            User user = (User) session.getAttribute("user");
            auth = true;
            role = user.getRole();

            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp lastLogin = user.getLastLogin();
            if(now.getTime() - lastLogin.getTime() > 1000*60) {
                UserEntity userEntity = usersRepository.findByEmail(user.getEmail()).orElse(null);
                if(userEntity != null) {
                    user.setLastLogin(now);
                    userEntity.setLastLogin(now);
                    usersRepository.save(userEntity);
                    session.setAttribute("user", user);
                }
            }
        }
        model.addAttribute("auth", auth);
        model.addAttribute("role", role);
    }
}
