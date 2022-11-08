package ru.mralexeimk.yedom.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.CourseRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.Pair;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.services.FriendsService;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/lk")
public class LkController {
    private final FriendsService friendsService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LkController(FriendsService friendsService, UserRepository userRepository, CourseRepository courseRepository, UserValidator userValidator, PasswordEncoder passwordEncoder) {
        this.friendsService = friendsService;
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

        Pair<String, String> pair = friendsService.DEFAULT_FOLLOW_BTN;
        User user = (User) session.getAttribute("user");

        if(username == null || username.equals(user.getUsername())) {
            UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
            if(userEntity == null) return "redirect:/errors/notfound";

            model.addAttribute("user", new User(userEntity));
            model.addAttribute("friends_count",
                    friendsService.getFriendsCount(userEntity));
        }
        else {
            UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
            if(userEntity == null) return "redirect:/errors/notfound";

            model.addAttribute("user", new User(userEntity));
            model.addAttribute("friends_count",
                    friendsService.getFriendsCount(userEntity));
            pair = friendsService.updateFollowBtn(user, userEntity);
        }

        model.addAttribute("action", pair.getFirst());
        model.addAttribute("color", pair.getSecond());

        return "lk/profile";
    }

    @GetMapping("/profile/friends")
    public String profileFriendsGet(Model model,
                                    @RequestParam(required = false, name = "username") String username,
                                    HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        List<Integer> friendsIds;

        UserEntity userEntity;
        if(username == null || username.equals(user.getUsername())) {
            userEntity = userRepository.findById(user.getId()).orElse(null);
        }
        else {
            userEntity = userRepository.findByUsername(username).orElse(null);
        }

        if(userEntity == null) return "redirect:/errors/notfound";
        friendsIds = friendsService.splitToListInt(userEntity.getFriendsIds());

        model.addAttribute("friends_count",
                friendsService.getFriendsCount(userEntity));

        model.addAttribute("friends",
                userRepository.findAllById(friendsIds));

        return "lk/profile/friends";
    }

    @GetMapping("/profile/courses")
    public String profileCoursesGet(Model model,
                             @RequestParam(required = false, name = "username") String username,
                             HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        List<CourseEntity> completedCourses, currentCourses;

        UserEntity userEntity;
        if(username == null || username.equals(user.getUsername())) {
            userEntity = userRepository.findById(user.getId()).orElse(null);
        }
        else {
            userEntity = userRepository.findByUsername(username).orElse(null);
        }

        if(userEntity == null) return "redirect:/errors/notfound";

        completedCourses = courseRepository.findAllById(
                friendsService.splitToListInt(userEntity.getCompletedCoursesIds()));
        currentCourses = courseRepository.findAllById(
                friendsService.splitToListInt(userEntity.getCurrentCoursesIds()));

        model.addAttribute("friends_count",
                friendsService.getFriendsCount(userEntity));

        model.addAttribute("completed_courses", completedCourses);
        model.addAttribute("current_courses", currentCourses);

        return "lk/profile/courses";
    }

    @GetMapping("/courses")
    public String coursesGet(HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        return "lk/courses";
    }

    @PostMapping("/profile/follow")
    public @ResponseBody ResponseEntity<Object> followBtn(@RequestParam(name = "username") String username,
                                                          HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);

        if(user.getUsername().equals(username) || userEntity == null)
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        friendsService.followPress(user, userEntity);

        return new ResponseEntity<>(HttpStatus.valueOf(200));
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

        return "redirect:/lk/account";
    }
}
