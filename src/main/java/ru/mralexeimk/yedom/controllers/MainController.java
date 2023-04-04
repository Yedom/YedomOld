package ru.mralexeimk.yedom.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.services.LoaderService;
import ru.mralexeimk.yedom.services.UtilsService;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class MainController {
    private final UtilsService utilsService;
    private final LoaderService loaderService;

    public MainController(UtilsService utilsService, LoaderService loaderService) {
        this.utilsService = utilsService;
        this.loaderService = loaderService;
    }

    @GetMapping()
    public String index() {
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

    /**
     * Get file uploading progress
     */
    @GetMapping(value = "/uploadingProgress", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String uploadingProgress(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        StringBuilder progress = new StringBuilder();
        User user = (User) session.getAttribute("user");
        progress.append(loaderService.getProgress(user.getId()));
        return utilsService.jsonToString(progress, "progress");
    }
}
