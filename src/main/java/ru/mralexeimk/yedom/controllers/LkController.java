package ru.mralexeimk.yedom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.CourseRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/lk")
public class LkController {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LkController(UserRepository userRepository, CourseRepository courseRepository, UserValidator userValidator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/account")
    public String accountGet(Model model, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        user.setPassword("");
        model.addAttribute("user", user);

        return "lk/account";
    }

    @GetMapping("/profile")
    public String profileGet(Model model,
                                 @RequestParam(required = false, name = "username") String username,
                                 HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        if(username == null) {
            User user = (User) session.getAttribute("user");
            model.addAttribute("user", user);
            model.addAttribute("my_profile", true);
        }
        else {
            UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
            if(userEntity == null) return "redirect:/errors/notfound";
            model.addAttribute("user", new User(userEntity));
            model.addAttribute("my_profile", false);
        }

        return "lk/profile";
    }

    @GetMapping("/profile/courses")
    public String profileCoursesGet(Model model,
                             @RequestParam(required = false, name = "username") String username,
                             HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        List<Integer> completedCoursesIds = new ArrayList<>();
        List<Integer> currentCoursesIds = new ArrayList<>();

        try {
            if (username == null) {
                User user = (User) session.getAttribute("user");
                completedCoursesIds = Arrays.stream(user.getCompletedCoursesIds().split(","))
                        .map(Integer::parseInt).toList();
                currentCoursesIds = Arrays.stream(user.getCurrentCoursesIds().split(","))
                        .map(Integer::parseInt).toList();
            } else {
                UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
                if (userEntity == null) return "redirect:/errors/notfound";

                completedCoursesIds = Arrays.stream(userEntity.getCompletedCoursesIds().split(","))
                        .map(Integer::parseInt).toList();
                currentCoursesIds = Arrays.stream(userEntity.getCurrentCoursesIds().split(","))
                        .map(Integer::parseInt).toList();
            }
        } catch (Exception ignored) {}

        model.addAttribute("completedCourses",
                courseRepository.findAllById(completedCoursesIds));
        model.addAttribute("currentCourses",
                courseRepository.findAllById(currentCoursesIds));

        return "lk/profile/courses";
    }

    @GetMapping("/profile/friends")
    public String profileFriendsGet(Model model,
                                    @RequestParam(required = false, name = "username") String username,
                                    HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        List<Integer> friendsIds = new ArrayList<>();

        try {
            if (username == null) {
                User user = (User) session.getAttribute("user");
                friendsIds = Arrays.stream(user.getFriendsIds().split(","))
                        .map(Integer::parseInt).toList();
            } else {
                UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
                if (userEntity == null) return "redirect:/errors/notfound";

                friendsIds = Arrays.stream(userEntity.getFriendsIds().split(","))
                        .map(Integer::parseInt).toList();
            }
        } catch (Exception ignored) {}

        model.addAttribute("friends",
                userRepository.findAllById(friendsIds));

        return "lk/profile/friends";
    }

    @GetMapping("/courses")
    public String coursesGet(HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;


        return "lk/courses";
    }

    @PostMapping
    public String accountPost(@ModelAttribute("user") User userModel,
                         @RequestParam(value = "log_out", required = false) String logOut,
                         HttpSession session, BindingResult bindingResult) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        if (logOut != null) {
            session.removeAttribute("user");
            return "redirect:auth/login";
        }

        User user = (User) session.getAttribute("user");
        user.setNewPassword(userModel.getNewPassword());
        user.setNewPasswordRepeat(userModel.getNewPasswordRepeat());
        user.setPassword(userModel.getPassword());
        user.setUsername(userModel.getUsername());

        UserEntity userEntity = userRepository.findByEmail(user.getEmail()).orElse(null);

        if(userEntity == null) {
            session.removeAttribute("user");
            return "redirect:auth/login";
        }

        userValidator.validate(user.withArgs("onUpdate"), bindingResult);

        if (bindingResult.hasErrors())
            return "lk/account";

        if(user.getNewPassword() != null && !user.getNewPassword().equals("")) {
            userEntity.setPassword(passwordEncoder.encode(user.getNewPassword()));
        }

        userEntity.setUsername(user.getUsername());

        userRepository.save(userEntity);
        session.setAttribute("user", user);

        session.removeAttribute("user");
        return "redirect:/lk/account";
    }
}
