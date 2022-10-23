package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.UserValidator;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;

@Controller
@RequestMapping("/lk")
public class LkController {
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LkController(UserRepository userRepository, UserValidator userValidator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping()
    public String lkGet(Model model, HttpSession session) {
        if(session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            model.addAttribute("user", user);
            return "lk/index";
        }
        return "redirect:auth/login";
    }

    @GetMapping("/{id}")
    public String lkUserGet(@PathVariable("id") @NotBlank String strId, Model model) {
        int id;
        try {
            id = Integer.parseInt(strId);
        } catch (NumberFormatException e) {
            return "errors/invalid";
        }
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        if(userEntity == null) {
            return "errors/notfound";
        }
        model.addAttribute("user", new User(userEntity));
        return "lk/user";
    }

    @PostMapping
    public String lkPost(@RequestBody String data, HttpSession session, BindingResult bindingResult) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("logout")) {
            session.removeAttribute("user");
            return "redirect:/";
        }
        if(operation.equalsIgnoreCase("save")) {
            User user = ((User) session.getAttribute("user")).clearArgs();
            UserEntity userEntity = userRepository.findByUsername(user.getUsername()).orElse(null);

            if(userEntity == null) {
                session.removeAttribute("user");
                return "redirect:/";
            }

            user.setUsername(json.getString("username"));
            //user.setPassword(json.getString("password"));
            userValidator.validate(user.addArg("onUpdate"), bindingResult);
            //user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (bindingResult.hasErrors())
                return "lk/index";
            userEntity.setUsername(user.getUsername());
            userRepository.save(userEntity);
            session.setAttribute("user", user);
        }
        return "redirect:lk/";
    }
}
