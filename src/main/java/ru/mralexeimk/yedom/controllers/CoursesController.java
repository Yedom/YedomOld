package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.interfaces.repositories.CourseRepository;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CoursesController {
    private final CourseRepository courseRepository;

    @Autowired
    public CoursesController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public String index(Model model, @RequestParam(required = false, name = "search") String search) {
        if(search == null) {
            model.addAttribute("courses", courseRepository.findTop10ByOrderByViewsDesc());
        }
        else {
            model.addAttribute("courses",
                    courseRepository.findByIdIn(List.of(1,4,8)));
        }
        return "courses/index";
    }

    @GetMapping("/add")
    public String add() {
        return "courses/add";
    }
}
