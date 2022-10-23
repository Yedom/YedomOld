package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.interfaces.repositories.CourseRepository;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.User;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
            model.addAttribute("courses", courseRepository.findByOrderByViewsDesc());
        }
        else {
            model.addAttribute("courses",
                    courseRepository.findByIdIn(List.of(1,4,8)));
        }
        return "courses/index";
    }

    @GetMapping("/{id}")
    public String lkUserGet(@PathVariable("id") @NotBlank String strId, Model model) {
        int id;
        try {
            id = Integer.parseInt(strId);
        } catch (NumberFormatException e) {
            return "errors/invalid";
        }
        CourseEntity courseEntity = courseRepository.findById(id).orElse(null);
        if(courseEntity == null) return "errors/notfound";
        model.addAttribute("course", new Course(courseEntity));
        return "courses/course";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("course") Course course, HttpSession session) {
        if(session.getAttribute("user") == null) {
            return "redirect:/auth/login";
        }
        return "courses/add";
    }

    @PostMapping("/add")
    public String addPost(@ModelAttribute("course") @Valid Course course,
                      HttpSession session) {
        if(session.getAttribute("user") == null) {
            return "redirect:/auth/login";
        }

        User user = (User) session.getAttribute("user");
        course.setAuthor(user.getUsername());
        course.setViews(0);
        course.setLikes(0);
        course.setSponsors("");

        CourseEntity courseEntity = new CourseEntity(course);

        courseRepository.save(courseEntity);

        return "redirect:/courses";
    }
}
