package ru.mralexeimk.yedom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class MainController {
    @GetMapping()
    public String startPage() {
        return "index";
    }
}
