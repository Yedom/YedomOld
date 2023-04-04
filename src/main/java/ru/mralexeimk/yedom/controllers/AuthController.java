package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.AuthConfig;
import ru.mralexeimk.yedom.config.configs.HostConfig;
import ru.mralexeimk.yedom.config.configs.ProfileConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.Code;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.enums.HashAlg;
import ru.mralexeimk.yedom.utils.enums.UserValidationType;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.services.EmailService;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * Controller for authentication process pages
 */
@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UtilsService utilsService;
    private final UsersRepository usersRepository;
    private final UserValidator userValidator;
    private final EmailService emailService;
    private final LanguageUtil languageUtil;
    private final PasswordEncoder passwordEncoder;
    private final HostConfig hostConfig;
    private final AuthConfig authConfig;
    private final ProfileConfig profileConfig;

    @Autowired
    public AuthController(UtilsService utilsService, UsersRepository usersRepository, UserValidator userValidator,
                          EmailService emailService, LanguageUtil languageUtil,
                          PasswordEncoder passwordEncoder, HostConfig hostConfig, AuthConfig authConfig, ProfileConfig profileConfig) {
        this.utilsService = utilsService;
        this.usersRepository = usersRepository;
        this.userValidator = userValidator;
        this.emailService = emailService;
        this.languageUtil = languageUtil;
        this.passwordEncoder = passwordEncoder;
        this.hostConfig = hostConfig;
        this.authConfig = authConfig;
        this.profileConfig = profileConfig;
    }

    /**
     * User registration page
     */
    @GetMapping("/reg")
    public String newPerson(@ModelAttribute("user") User user) {
        return "auth/reg";
    }

    /**
     * User login page
     */
    @GetMapping("/login")
    public String authPerson(@ModelAttribute("user") User user) {
        return "auth/login";
    }

    /**
     * User confirm email page
     */
    @GetMapping("/confirm")
    public String confirmPerson(@ModelAttribute("code") Code code) {
        return "auth/confirm";
    }

    /**
     * User restore password by email page
     */
    @GetMapping("/restore")
    public String restorePassword() {
        return "auth/restore";
    }

    /**
     * User change password page (after restore)
     */
    @GetMapping("/newpassword")
    public String newPassword(HttpSession session) {
        if(session.getAttribute("newpassword") != null) {
            return "auth/newpassword";
        }
        return "auth/restore";
    }

    /**
     * Restore password link from email
     */
    @GetMapping("/restore/{hashedCode}")
    public String confirmChangePassword(@PathVariable(value = "hashedCode") String hashedCode, HttpSession session) {
        if (session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");
            UserEntity userEntity = usersRepository.findByEmail(email).orElse(null);
            if(userEntity == null)
                return "redirect:/auth/restore";
            if(!emailService.getCodeByUser().containsKey(userEntity.getUsername()))
                return "redirect:/auth/restore";
            try {
                String hashedCodeActual = utilsService.hash(
                        emailService.getCodeByUser().get(userEntity.getUsername()).getCode(),
                        HashAlg.SHA256);
                if (hashedCodeActual.equals(hashedCode)) {
                    session.setAttribute("newpassword", true);
                    return "auth/newpassword";
                }
            } catch (Exception ignored) {}
        }
        return "redirect:/auth/restore";
    }

    /**
     * Apply new password
     */
    @PostMapping("/newpassword")
    public @ResponseBody ResponseEntity<Object> newPasswordApply(@RequestBody String data,
                                                                                  HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("newpassword")) {
            String password = json.getString("password");
            if(password.length() < authConfig.getMinPasswordLength()) {
                return new ResponseEntity<>(HttpStatus.valueOf(501));
            }
            if(password.length() > authConfig.getMaxPasswordLength()) {
                return new ResponseEntity<>(HttpStatus.valueOf(502));
            }
            try {
                String email = (String) session.getAttribute("email");
                UserEntity userEntity = usersRepository.findByEmail(email).orElse(null);
                if (userEntity != null) {
                    userEntity.setPassword(passwordEncoder.encode(password));
                    usersRepository.save(userEntity);
                    session.removeAttribute("newpassword");
                    session.removeAttribute("email");
                }
                else return new ResponseEntity<>(HttpStatus.valueOf(500));
            } catch (Exception ex) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }
        }
        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Generate restore password link and send it to email
     */
    @PostMapping("/restore")
    public @ResponseBody ResponseEntity<Object> changePassword(@RequestBody String data,
                                                               HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("restore")) {
            String email = json.getString("email");
            try {
                UserEntity userEntity = usersRepository.findByEmail(email).orElse(null);
                if(userEntity != null) {
                    emailService.saveCode(userEntity.getUsername(), emailService.getRandomCode());
                    String link = hostConfig.getLink() + "auth/restore/" +
                            utilsService.hash(
                                    emailService.getCodeByUser().get(userEntity.getUsername()).getCode(),
                                    HashAlg.SHA256);
                    session.setAttribute("email", email);
                    emailService.sendMessage(email,
                            languageUtil.getLocalizedMessage("auth.mail.restore.title"),
                            languageUtil.getLocalizedMessage("auth.mail.restore.body",
                                    link));
                }
                else return new ResponseEntity<>(HttpStatus.valueOf(500));
            } catch (Exception ex) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }
        }
        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Send code to confirm email
     */
    @PostMapping("/reg")
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult bindingResult, HttpSession session) {
        userValidator.validate(user.checkFor(UserValidationType.REG), bindingResult);

        if (bindingResult.hasErrors())
            return "auth/reg";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        session.setAttribute("user", user);

        Code code = emailService.getRandomCode();

        System.out.println("code: " + code);
        emailService.saveCode(user.getUsername(), code);
        emailService.sendMessage(user.getEmail(),
                languageUtil.getLocalizedMessage("auth.mail.title"),
                languageUtil.getLocalizedMessage("auth.mail.body", code.getCode())
        );

        return "redirect:confirm";
    }

    /**
     * User login
     */
    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") User user,
                            BindingResult bindingResult, HttpSession session) {
        userValidator.validate(user.checkFor(UserValidationType.LOGIN), bindingResult);
        UserEntity userEntity = usersRepository.findByEmail(user.getUsername()).orElse(null);

        if (bindingResult.hasErrors() && userEntity == null)
            return "auth/login";

        if(userEntity == null) {
            userEntity = usersRepository.findByUsername(user.getUsername()).orElse(null);
        }

        if(userEntity == null) return "auth/login";

        userEntity.setLastLogin(utilsService.getCurrentTimestamp());
        usersRepository.save(userEntity);

        User cloneUser = new User(userEntity);
        session.setAttribute("user", cloneUser);

        return "redirect:/";
    }

    /**
     * User email confirmation to complete registration
     */
    @PostMapping("/confirm")
    public String confirmUser(@ModelAttribute("code") @Valid Code code,
                              BindingResult bindingResult, HttpSession session) {
        if(!emailService.isCorrectCode(code.getCode())) {
            bindingResult.rejectValue("code", "",
                    languageUtil.getLocalizedMessage("auth.confirm.fail"));
            return "auth/confirm";
        }
        if (session.getAttribute("user") == null) {
            bindingResult.rejectValue("code", "",
                    languageUtil.getLocalizedMessage("auth.session.expired"));
            return "auth/confirm";
        }
        User user = (User) session.getAttribute("user");
        if(!emailService.getCodeByUser().containsKey(user.getUsername()) ||
                !emailService.getCodeByUser()
                        .get(user.getUsername()).getCode().equals(code.getCode())) {
            bindingResult.rejectValue("code", "",
                    languageUtil.getLocalizedMessage("auth.confirm.deny"));
            return "auth/confirm";
        }

        user.setEmailConfirmed(true);
        user.setCreatedOn(utilsService.getCurrentTimestamp());
        user.setLastLogin(utilsService.getCurrentTimestamp());
        user.setAvatar(profileConfig.getBaseAvatarDefault());

        UserEntity userEntity = new UserEntity(user);

        usersRepository.save(userEntity);
        user = new User(userEntity);
        session.setAttribute("user", user);
        emailService.removeCode(user.getUsername());

        if(bindingResult.hasErrors()) {
            return "auth/confirm";
        }

        return "redirect:/";
    }
}
