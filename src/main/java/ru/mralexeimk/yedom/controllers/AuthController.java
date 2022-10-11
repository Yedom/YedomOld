package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.SpringConfig;
import ru.mralexeimk.yedom.database.UserDB;
import ru.mralexeimk.yedom.interfaces.validation.OrderedChecks;
import ru.mralexeimk.yedom.models.Code;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.EmailService;
import ru.mralexeimk.yedom.utils.LanguageUtil;
import ru.mralexeimk.yedom.utils.UserValidator;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserDB userDB;
    private final UserValidator userValidator;
    private final EmailService emailService;
    private final LanguageUtil languageUtil;

    @Autowired
    public AuthController(UserDB userDB, UserValidator userValidator, EmailService emailService, LanguageUtil languageUtil) {
        this.userDB = userDB;
        this.userValidator = userValidator;
        this.emailService = emailService;
        this.languageUtil = languageUtil;
    }

    @GetMapping("/reg")
    public String newPerson(@ModelAttribute("user") User user) {
        return "auth/reg";
    }

    @GetMapping("/login")
    public String authPerson(@ModelAttribute("user") User user) {
        return "auth/login";
    }

    @GetMapping("/confirm")
    public String confirmPerson(@ModelAttribute("code") Code code) {
        return "auth/confirm";
    }

    @GetMapping("/restore")
    public String restorePassword(@ModelAttribute("email") String email) {
        return "auth/restore";
    }

    @GetMapping("/newpassword")
    public String newPassword(@ModelAttribute("password") String password, HttpSession session) {
        if(session.getAttribute("newpassword") != null) {
            return "auth/newpassword";
        }
        return "redirect:/auth/restore";
    }

    @GetMapping("/restore/{baseCode}")
    public String confirmChangePassword(@PathVariable String baseCode, HttpSession session) {
        if (session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");
            User user = userDB.getUserByEmail(email);
            if(user != null) {
                if (EmailService.getCodeByUser().containsKey(user)) {
                    try {
                        String baseCodeActual = CommonUtils.hashEncoder(EmailService.getCodeByUser().get(user).getCode());
                        if (baseCodeActual.equals(baseCode)) {
                            session.setAttribute("newpassword", true);
                            return "auth/newpassword";
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return "redirect:/auth/restore";
    }

    @PostMapping("/newpassword")
    public String newPasswordApply(@RequestBody String data, HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("newpassword")) {
            String password = json.getString("password");
            try {
                String email = (String) session.getAttribute("email");
                User user = userDB.getUserByEmail(email);
                user.setPassword(password);
                userDB.updateById(user.getId(), user);
                session.removeAttribute("newpassword");
                session.removeAttribute("email");
            } catch (Exception ignored) { }
        }
        return "redirect:/auth/login";
    }

    @PostMapping("/restore")
    public String changePassword(@RequestBody String data, BindingResult bindingResult, HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("restore")) {
            String email = json.getString("email");
            try {
                User user = userDB.getUserByEmail(email);
                EmailService.saveCode(user, EmailService.getRandomCode());
                String link =  SpringConfig.DOMAIN +
                        CommonUtils.hashEncoder(EmailService.getCodeByUser().get(user).getCode());
                session.setAttribute("email", email);
                emailService.sendMessage(email,
                        languageUtil.getLocalizedMessage("auth.mail.restore.title"),
                        languageUtil.getLocalizedMessage("auth.mail.restore.body",
                                link));
            } catch (Exception ex) {
                bindingResult.rejectValue("email", "",
                        languageUtil.getLocalizedMessage("auth.email.incorrect"));
            }
        }
        return "redirect:auth/restore";
    }

    @PostMapping("/reg")
    public String registerUser(@ModelAttribute("user") @Validated(OrderedChecks.class) User user,
                               BindingResult bindingResult, HttpSession session) {
        userValidator.validate(user.addArg("onReg"), bindingResult);

        if (bindingResult.hasErrors())
            return "auth/reg";

        session.setAttribute("user", user);

        Code code = EmailService.getRandomCode();

        EmailService.saveCode(user, code);
        emailService.sendMessage(user.getEmail(),
                languageUtil.getLocalizedMessage("auth.mail.title"),
                languageUtil.getLocalizedMessage("auth.mail.body", code.getCode())
        );

        return "redirect:confirm";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") User user,
                            BindingResult bindingResult, HttpSession session) {

        userValidator.validate(user.addArg("onLogin"), bindingResult);

        if (bindingResult.hasErrors())
            return "auth/login";

        User logUser = userDB.getUserByUsername(user.getUsername());
        logUser.setLastLogin(CommonUtils.getCurrentTimestamp());

        session.setAttribute("user", logUser);

        return "redirect:/";
    }

    @PostMapping("/confirm")
    public String confirmUser(@ModelAttribute("code") @Valid Code code,
                              BindingResult bindingResult, HttpSession session) {
        if(EmailService.isCorrectCode(code.getCode())) {
            if (session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                if (EmailService.getCodeByUser().containsKey(user) &&
                        EmailService.getCodeByUser().get(user).getCode().equals(code.getCode())) {
                    user.setCreatedOn(CommonUtils.getCurrentTimestamp());
                    user.setLastLogin(CommonUtils.getCurrentTimestamp());
                    userDB.save(user);
                    session.setAttribute("user", user);
                    EmailService.removeCode(user);
                } else bindingResult.rejectValue("code", "",
                        languageUtil.getLocalizedMessage("auth.confirm.deny"));
            } else bindingResult.rejectValue("code", "",
                    languageUtil.getLocalizedMessage("auth.session.expired"));
        }
        else bindingResult.rejectValue("code", "",
                languageUtil.getLocalizedMessage("auth.confirm.fail"));

        if(bindingResult.hasErrors()) {
            return "auth/confirm";
        }

        return "redirect:/";
    }
}
