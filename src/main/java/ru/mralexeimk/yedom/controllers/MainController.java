package ru.mralexeimk.yedom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.models.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@RequestMapping("/")
public class MainController {
    @GetMapping()
    public String index(Model model, HttpSession session) {
        return "index";
    }

    /**
     * robots.txt to allow browser to crawl the site
     */
    @RequestMapping(value = "/robots.txt")
    public void robots(HttpServletResponse response) {
        try {
            response.getWriter().write("User-agent: *\nDisallow: /\n");
        } catch (IOException ignored) {}
    }

    /**
     * Favicon on browser tab
     */
    @RequestMapping("favicon.ico")
    String favicon() {
        return "forward:/resources/favicon.ico";
    }
}
