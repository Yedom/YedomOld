package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.utils.interfaces.validation.OrderedChecks;
import ru.mralexeimk.yedom.models.Code;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.services.EmailService;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.services.TagsService;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final EmailService emailService;
    private final LanguageUtil languageUtil;
    private final PasswordEncoder passwordEncoder;
    private final TagsService tagsService;

    @Autowired
    public AuthController(UserRepository userRepository, UserValidator userValidator,
                          EmailService emailService, LanguageUtil languageUtil,
                          PasswordEncoder passwordEncoder, TagsService tagsService) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.emailService = emailService;
        this.languageUtil = languageUtil;
        this.passwordEncoder = passwordEncoder;
        this.tagsService = tagsService;
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
    public String restorePassword() {
        return "auth/restore";
    }

    @GetMapping("/newpassword")
    public String newPassword(HttpSession session) {
        if(session.getAttribute("newpassword") != null) {
            return "auth/newpassword";
        }
        return "auth/restore";
    }

    @GetMapping("/restore/{baseCode}")
    public String confirmChangePassword(@PathVariable String baseCode, HttpSession session) {
        if (session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");
            UserEntity userEntity = userRepository.findByEmail(email).orElse(null);
            if(userEntity != null) {
                if (emailService.getCodeByUser().containsKey(userEntity.getUsername())) {
                    try {
                        String baseCodeActual = CommonUtils.hashEncoder(
                                emailService.getCodeByUser().get(userEntity.getUsername()).getCode());
                        if (baseCodeActual.equals(baseCode)) {
                            session.setAttribute("newpassword", true);
                            return "auth/newpassword";
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return "redirect:auth/restore";
    }

    @PostMapping("/newpassword")
    public @ResponseBody ResponseEntity<Object> newPasswordApply(@RequestBody String data,
                                                                                  HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("newpassword")) {
            String password = json.getString("password");
            if(password.length() < YedomConfig.minPasswordLength) {
                return new ResponseEntity<>(HttpStatus.valueOf(501));
            }
            try {
                String email = (String) session.getAttribute("email");
                UserEntity userEntity = userRepository.findByEmail(email).orElse(null);
                if (userEntity != null) {
                    userEntity.setPassword(passwordEncoder.encode(password));
                    userRepository.save(userEntity);
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

    @PostMapping("/restore")
    public @ResponseBody ResponseEntity<Object> changePassword(@RequestBody String data,
                                                               HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("restore")) {
            String email = json.getString("email");
            try {
                UserEntity userEntity = userRepository.findByEmail(email).orElse(null);
                if(userEntity != null) {
                    emailService.saveCode(userEntity.getUsername(), emailService.getRandomCode());
                    String link = YedomConfig.DOMAIN + "auth/restore/" +
                            CommonUtils.hashEncoder(
                                    emailService.getCodeByUser().get(userEntity.getUsername()).getCode());
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

    @PostMapping("/reg")
    public String registerUser(@ModelAttribute("user") @Validated(OrderedChecks.class) User user,
                               BindingResult bindingResult, HttpSession session) {
        userValidator.validate(user.withArgs("onReg"), bindingResult);

        if (bindingResult.hasErrors())
            return "auth/reg";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        session.setAttribute("user", user);

        Code code = emailService.getRandomCode();

        emailService.saveCode(user.getUsername(), code);
        emailService.sendMessage(user.getEmail(),
                languageUtil.getLocalizedMessage("auth.mail.title"),
                languageUtil.getLocalizedMessage("auth.mail.body", code.getCode())
        );

        return "redirect:confirm";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") User user,
                            BindingResult bindingResult, HttpSession session) {
        userValidator.validate(user.withArgs("onLogin"), bindingResult);
        UserEntity userEntity = userRepository.findByEmail(user.getUsername()).orElse(null);

        if (bindingResult.hasErrors() && userEntity == null)
            return "auth/login";

        if(userEntity == null) {
            userEntity = userRepository.findByUsername(user.getUsername()).orElse(null);
        }

        if(userEntity == null) return "auth/login";

        userEntity.setLastLogin(CommonUtils.getCurrentTimestamp());
        userRepository.save(userEntity);

        user = new User(userEntity);
        session.setAttribute("user", user);
        tagsService.createConnection(user);

        return "redirect:/";
    }

    @PostMapping("/confirm")
    public String confirmUser(@ModelAttribute("code") @Valid Code code,
                              BindingResult bindingResult, HttpSession session) {
        if(emailService.isCorrectCode(code.getCode())) {
            if (session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                if (emailService.getCodeByUser().containsKey(user.getUsername()) &&
                        emailService.getCodeByUser()
                                .get(user.getUsername()).getCode().equals(code.getCode())) {
                    user.setEmailConfirmed(true);
                    UserEntity userEntity = new UserEntity(user);
                    userRepository.save(userEntity);
                    session.setAttribute("user", new User(userEntity));
                    emailService.removeCode(user.getUsername());
                    tagsService.createConnection(user);
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
