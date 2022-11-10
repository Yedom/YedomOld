package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.ProfileConfig;
import ru.mralexeimk.yedom.database.entities.*;
import ru.mralexeimk.yedom.database.repositories.*;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.services.FriendsService;
import ru.mralexeimk.yedom.utils.validators.UserValidator;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/lk")
public class LkController {
    private final FriendsService friendsService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CompletedCoursesRepository completedCoursesRepository;
    private final CourseRepository courseRepository;
    private final DraftCourseRepository draftCourseRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final ProfileConfig profileConfig;

    @Autowired
    public LkController(FriendsService friendsService, UserRepository userRepository, OrganizationRepository organizationRepository, CompletedCoursesRepository completedCoursesRepository, CourseRepository courseRepository, DraftCourseRepository draftCourseRepository, UserValidator userValidator, PasswordEncoder passwordEncoder, ProfileConfig profileConfig) {
        this.friendsService = friendsService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.completedCoursesRepository = completedCoursesRepository;
        this.courseRepository = courseRepository;
        this.draftCourseRepository = draftCourseRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
        this.profileConfig = profileConfig;
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

    private Pair<UserEntity, Boolean> getUserEntity(String username, User user) {
        UserEntity userEntity;
        boolean isSelf = false;
        if(username == null || username.equals(user.getUsername())) {
            userEntity = userRepository.findById(user.getId()).orElse(null);
            isSelf = true;
        }
        else {
            userEntity = userRepository.findByUsername(username).orElse(null);
        }
        return new Pair<>(userEntity, isSelf);
    }

    @GetMapping("/profile")
    public String profileGet(Model model,
                                 @RequestParam(required = false, name = "username") String username,
                                 HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        Pair<String, String> pair = friendsService.DEFAULT_FOLLOW_BTN;

        Pair<UserEntity, Boolean> profile = getUserEntity(username, user);
        UserEntity userEntity = profile.getFirst();
        boolean isSelf = profile.getSecond();

        if(userEntity == null) return "redirect:/errors/notfound";

        model.addAttribute("user", new User(userEntity));
        model.addAttribute("friends_count",
                friendsService.getFriendsCount(userEntity));

        if(!isSelf) {
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

        Pair<UserEntity, Boolean> profile = getUserEntity(username, user);
        UserEntity userEntity = profile.getFirst();

        if(userEntity == null) return "redirect:/errors/notfound";
        List<Integer> friendsIds = CommonUtils.splitToListInt(userEntity.getFriendsIds());

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

        Pair<UserEntity, Boolean> profile = getUserEntity(username, user);
        UserEntity userEntity = profile.getFirst();

        if(userEntity == null) return "redirect:/errors/notfound";

        List<CompletedCoursesEntity> completedCoursesEntities =
                completedCoursesRepository.findAllByUserIdOrderByCompletedOnDesc(userEntity.getId());

        List<Course> completedCourses = new ArrayList<>();

        for(CompletedCoursesEntity completedCoursesEntity : completedCoursesEntities) {
            CourseEntity courseEntity = courseRepository.findById(completedCoursesEntity.getCourseId()).orElse(null);
            if(courseEntity == null) continue;
            Course course = new Course(courseEntity);
            course.setCompletedOn(completedCoursesEntity.getCompletedOn());
            course.setTags(course.getTags().replaceAll("@", ", "));
            if(!course.isByOrganization()) {
                course.setCreatorName(Objects.requireNonNull(
                        userRepository.findById(courseEntity.getCreatorId()).orElse(null)).getUsername());
            }
            else {
                course.setCreatorName(Objects.requireNonNull(
                        organizationRepository.findById(courseEntity.getCreatorId()).orElse(null)).getName());
            }
            completedCourses.add(course);
        }

        model.addAttribute("friends_count",
                friendsService.getFriendsCount(userEntity));

        model.addAttribute("completed_courses", completedCourses);

        return "lk/profile/courses";
    }

    @GetMapping("/profile/organizations")
    public String profileOrganizationsGet(Model model,
                                    @RequestParam(required = false, name = "username") String username,
                                    HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        Pair<UserEntity, Boolean> profile = getUserEntity(username, user);
        UserEntity userEntity = profile.getFirst();

        if(userEntity == null) return "redirect:/errors/notfound";

        List<Organization> organizations = new ArrayList<>();

        for(Integer id : CommonUtils.splitToListInt(userEntity.getOrganizationsIds())) {
            OrganizationEntity organizationEntity = organizationRepository.findById(id).orElse(null);
            if(organizationEntity == null) continue;
            organizations.add(new Organization(organizationEntity));
        }

        model.addAttribute("friends_count",
                friendsService.getFriendsCount(userEntity));
        model.addAttribute("organizations", organizations);

        return "lk/profile/organizations";
    }

    @GetMapping("/profile/balance")
    public String profileBalanceGet(Model model,
                                          @RequestParam(required = false, name = "username") String username,
                                          HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        Pair<UserEntity, Boolean> profile = getUserEntity(username, user);
        UserEntity userEntity = profile.getFirst();
        boolean isSelf = profile.getSecond();

        if(userEntity == null || !isSelf) return "redirect:/errors/notfound";

        model.addAttribute("friends_count",
                friendsService.getFriendsCount(userEntity));
        model.addAttribute("user",
                new User(userEntity));

        return "lk/profile/balance";
    }

    @GetMapping("/draftCourses")
    public String coursesGet(Model model, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        List<DraftCourse> draftCourses = new ArrayList<>();

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/errors/notfound";

        for(Integer id : CommonUtils.splitToListInt(userEntity.getDraftCoursesIds())) {
            DraftCourseEntity draftCourseEntity = draftCourseRepository.findById(id).orElse(null);
            if(draftCourseEntity == null) continue;
            draftCourses.add(new DraftCourse(draftCourseEntity));
        }

        model.addAttribute("draft_courses", draftCourses);

        return "lk/draftCourses";
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

    @PostMapping("/profile/uploadAvatar")
    public @ResponseBody ResponseEntity<Object> uploadAvatar(@RequestBody String data,
                                                          HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);

            String baseImg = json.getString("baseImg");

            if(baseImg.length() > profileConfig.getBaseAvatarMaxSize() ||
                    !baseImg.split(",")[0].split("/")[0].equals("data:image")) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }

            userEntity.setAvatar(baseImg);
            userRepository.save(userEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

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
