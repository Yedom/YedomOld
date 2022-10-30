package ru.mralexeimk.yedom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;

@Controller
@RequestMapping("/lk")
public class LkController {
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final LanguageUtil languageUtil;

    @Autowired
    public LkController(UserRepository userRepository, UserValidator userValidator, PasswordEncoder passwordEncoder, LanguageUtil languageUtil) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
        this.languageUtil = languageUtil;
    }

    @GetMapping("/account")
    public String accountGet(Model model, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        user.setPassword("");
        model.addAttribute("user", user);

        return "lk/account";
    }

    @GetMapping("/profile")
    public String profileGet(Model model, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        return "lk/profile";
    }

    @GetMapping("/courses")
    public String coursesGet(HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;


        return "lk/courses";
    }

    @PostMapping
    public String accountPost(@ModelAttribute("user") User userModel,
                         @RequestParam(value = "log_out", required = false) String logOut,
                         HttpSession session, BindingResult bindingResult) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
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

        UserEntity userEntity = userRepository.findByEmail(user.getEmail()).orElse(null);

        if(userEntity == null) {
            session.removeAttribute("user");
            return "redirect:auth/login";
        }

        userValidator.validate(user.withArgs("onUpdate"), bindingResult);

        if (bindingResult.hasErrors())
            return "lk/account";

        user.setPassword(passwordEncoder.encode(user.getNewPassword()));

        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());

        userRepository.save(userEntity);
        session.setAttribute("user", user);

        session.removeAttribute("user");
        return "redirect:/lk/account";
    }
}
