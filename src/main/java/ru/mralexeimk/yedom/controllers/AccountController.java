package ru.mralexeimk.yedom.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.enums.UserValidationType;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

/**
 * Controller for account page (where user can change username and password)
 */
@Controller
@RequestMapping("/account")
public class AccountController {
    private final UtilsService utilsService;
    private final UsersRepository usersRepository;;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(UtilsService utilsService, UsersRepository usersRepository, UserValidator userValidator, PasswordEncoder passwordEncoder) {
        this.utilsService = utilsService;
        this.usersRepository = usersRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Account user page
     */
    @GetMapping
    public String accountGet(Model model, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        user.setPassword(""); // to prevent password from showing in form
        model.addAttribute("user", user);

        return "auth/account";
    }

    /**
     * User 'save changes'/'logout' buttons handler
     */
    @PostMapping
    public String accountPost(@ModelAttribute("user") User userModel,
                              @RequestParam(value = "log_out", required = false) String logOut,
                              HttpSession session, BindingResult bindingResult) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        if (logOut != null) {
            session.removeAttribute("user");
            return "redirect:auth/login";
        }

        User user = (User) session.getAttribute("user");
        user.setNewPassword(userModel.getNewPassword());
        user.setNewPasswordRepeat(userModel.getNewPasswordRepeat());
        user.setPassword(userModel.getPassword());
        user.setUsername(userModel.getUsername());

        UserEntity userEntity = usersRepository.findByEmail(user.getEmail()).orElse(null);

        if(userEntity == null) {
            session.removeAttribute("user");
            return "redirect:auth/login";
        }

        userValidator.validate(user.checkFor(UserValidationType.UPDATE), bindingResult);

        if (bindingResult.hasErrors())
            return "auth/account";

        if(user.getNewPassword() != null && !user.getNewPassword().equals("")) {
            userEntity.setPassword(passwordEncoder.encode(user.getNewPassword()));
        }

        userEntity.setUsername(user.getUsername());

        usersRepository.save(userEntity);
        session.setAttribute("user", user);

        return "redirect:/account";
    }
}
