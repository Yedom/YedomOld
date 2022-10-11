package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.UserDB;
import ru.mralexeimk.yedom.models.User;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;

@Controller
@RequestMapping("/lk")
public class LkController {
    private final UserDB userDB;

    @Autowired
    public LkController(UserDB userDB) {
        this.userDB = userDB;
    }

    @GetMapping()
    public String lkGet(Model model, HttpSession session) {
        if(session.getAttribute("user") != null) {
            model.addAttribute("user", session.getAttribute("user"));
            return "lk/home";
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
        User user = userDB.getUserById(id);
        if(user == null) {
            return "errors/notfound";
        }
        model.addAttribute("user", user);
        return "lk/user";
    }

    @PostMapping
    public String lkPost(@RequestBody String data, HttpSession session) {
        JSONObject json = new JSONObject(data);
        String operation = json.getString("operation");
        if(operation.equalsIgnoreCase("logout")) {
            session.removeAttribute("user");
            return "redirect:/";
        }
        if(operation.equalsIgnoreCase("save")) {
            User user = (User) session.getAttribute("user");
            user = userDB.getUserByUsername(user.getUsername());
            user.setUsername(json.getString("username"));
            userDB.updateById(user.getId(), user);
            session.setAttribute("user", user);
        }
        return "redirect:lk/";
    }
}
