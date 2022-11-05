package ru.mralexeimk.yedom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/")
public class MainController {
    @GetMapping()
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/robots.txt")
    public void robots(HttpServletResponse response) {
        try {
            response.getWriter().write("User-agent: *\nDisallow: /\n");
        } catch (IOException ignored) {}
    }

    @RequestMapping("favicon.ico")
    String favicon() {
        return "forward:/resources/favicon.ico";
    }
}
