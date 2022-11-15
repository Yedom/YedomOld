package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.ProfileConfig;
import ru.mralexeimk.yedom.database.entities.CompletedCoursesEntity;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.CompletedCoursesRepository;
import ru.mralexeimk.yedom.database.repositories.CourseRepository;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.*;
import ru.mralexeimk.yedom.utils.custom.Triple;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.custom.Pair;
import ru.mralexeimk.yedom.utils.services.FriendsService;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UtilsService utilsService;
    private final FriendsService friendsService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationsService organizationsService;
    private final CompletedCoursesRepository completedCoursesRepository;
    private final CourseRepository courseRepository;
    private final ProfileConfig profileConfig;
    private final LanguageUtil languageUtil;

    public ProfileController(UtilsService utilsService, FriendsService friendsService, UserRepository userRepository, OrganizationRepository organizationRepository, OrganizationsService organizationsService, CompletedCoursesRepository completedCoursesRepository, CourseRepository courseRepository, ProfileConfig profileConfig, LanguageUtil languageUtil) {
        this.utilsService = utilsService;
        this.friendsService = friendsService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.organizationsService = organizationsService;
        this.completedCoursesRepository = completedCoursesRepository;
        this.courseRepository = courseRepository;
        this.profileConfig = profileConfig;
        this.languageUtil = languageUtil;
    }

    /**
     * Get UserEntity of visited profile
     * @return Pair of UserEntity and Boolean (my profile or not)
     */
    private Triple<UserEntity, User, Boolean> getUserEntity(String username, HttpSession session) {
        User user = null;
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check == null) user = (User) session.getAttribute("user");

        UserEntity userEntity;
        boolean isSelf = false;

        // get my profile
        if(user != null && (username == null || username.equals(user.getUsername()))) {
            userEntity = userRepository.findById(user.getId()).orElse(null);
            isSelf = true;
        }
        // get another user profile
        else {
            userEntity = userRepository.findByUsername(username).orElse(null);
        }
        return new Triple<>(userEntity, user, isSelf);
    }

    /**
     * Get count of friends/followers/following/completed courses/organizations in Amounts class
     */
    private Amounts getAmounts(UserEntity userEntity, HttpServletRequest request) {
        Amounts amounts = new Amounts();
        try {
            amounts.setFriendsCount(friendsService.getFriendsCount(request, userEntity.getId()));
            amounts.setFollowersCount(friendsService.getFollowersCount(request, userEntity.getId()));
            amounts.setFollowingCount(friendsService.getFollowingsCount(request, userEntity.getId()));
        } catch (Exception e) {
            amounts.setFriendsCount(
                    utilsService.splitToListString(userEntity.getFriendsIds()).size());
            amounts.setFollowersCount(
                    utilsService.splitToListString(userEntity.getFollowersIds()).size());
            amounts.setFollowingCount(
                    utilsService.splitToListString(userEntity.getFollowingIds()).size());
        }
        amounts.setCompletedCoursesCount(completedCoursesRepository.countAllByUserId(userEntity.getId()));
        amounts.setOrganizationsCount(organizationsService.getOrganizationsInCount(userEntity));

        return amounts;
    }

    private Boolean[] modelAddSettings(Model model, UserEntity profileUser,
                                       User user, HttpServletRequest request) {
        Boolean[] res = new Boolean[4];
        String relation = "strangers";
        if(user != null) {
            if(profileUser.getId() != user.getId()) {
                if(friendsService.getFriendsList(request, user.getId()).contains(profileUser.getId())) {
                    relation = "friends";
                }
            }
            else relation = "self";
        }

        UserSettings settings = new UserSettings(profileUser.getSettings());
        res[0] = relation.equals("self")
                || (relation.equals("strangers") && settings.isStrangersShowEmail())
                || (relation.equals("friends") && settings.isFriendsShowEmail());
        res[1] = relation.equals("self")
                || (relation.equals("strangers") && settings.isStrangersShowLinks())
                || (relation.equals("friends") && settings.isFriendsShowLinks());
        res[2] = relation.equals("self")
                || (relation.equals("strangers") && settings.isStrangersShowOrganizations())
                || (relation.equals("friends") && settings.isFriendsShowOrganizations());
        res[3] = relation.equals("self")
                || (relation.equals("strangers") && settings.isStrangersShowCompletedCourses())
                || (relation.equals("friends") && settings.isFriendsShowCompletedCourses());

        model.addAttribute("show_email", res[0]);
        model.addAttribute("show_links", res[1]);
        model.addAttribute("show_organizations", res[2]);
        model.addAttribute("show_courses", res[3]);

        return res;
    }

    /**
     * Redirect .../profile to .../profile/{my username} if authorized
     */
    @GetMapping
    public String index(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User user = (User) session.getAttribute("user");

        return "redirect:/profile/" + user.getUsername();
    }

    /**
     * Get profile page of user
     * (Authorize is not required)
     */
    @GetMapping("/{username}")
    public String profileGet(Model model,
                             @PathVariable(value = "username") String username,
                             HttpServletRequest request, HttpSession session) {
        Pair<String, String> pair = friendsService.DEFAULT_FOLLOW_BTN;

        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        }

        modelAddSettings(model, userEntity, user, request);

        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity, request));

        // user is authorized
        if(user != null) {
            model.addAttribute("session_username", user.getUsername());
            // not user's profile
            if(user.getId() != userEntity.getId()) {
                pair = friendsService.getFollowButtonType(request, user.getId(), userEntity.getId());
            }
        }

        model.addAttribute("btn_properties", pair);

        return "profile/index";
    }

    /**
     * User friends/followers/following pages
     */
    @GetMapping("/{username}/{type}")
    public String profileFriendsGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    @PathVariable(value = "type") String type,
                                    HttpSession session, HttpServletRequest request) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        }
        modelAddSettings(model, userEntity, user, request);

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity, request));

        try {
            switch (type) {
                case "friends" -> model.addAttribute("users",
                        userRepository.findAllById(
                                friendsService.getFriendsList(request, userEntity.getId())));
                case "followers" -> model.addAttribute("users",
                        userRepository.findAllById(
                                friendsService.getFollowersList(request, userEntity.getId())));
                case "following" -> model.addAttribute("users",
                        userRepository.findAllById(
                                friendsService.getFollowingsList(request, userEntity.getId())));
            }
        } catch (Exception e) {
            switch (type) {
                case "friends" -> model.addAttribute("users",
                        userRepository.findAllById(
                                utilsService.splitToListInt(userEntity.getFriendsIds())));
                case "followers" -> model.addAttribute("users",
                        userRepository.findAllById(
                                utilsService.splitToListInt(userEntity.getFollowersIds())));
                case "following" -> model.addAttribute("users",
                        userRepository.findAllById(
                                utilsService.splitToListInt(userEntity.getFollowingIds())));
            }
        }

        return "profile/friends";
    }

    /**
     * User completed courses page
     */
    @GetMapping("/{username}/courses")
    public String profileCoursesGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    HttpSession session, HttpServletRequest request) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        };
        Boolean[] ch = modelAddSettings(model, userEntity, user, request);
        if(!ch[3]) {
            return "redirect:/profile/" + userEntity.getUsername();
        }

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

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity, request));

        model.addAttribute("completed_courses", completedCourses);

        return "profile/courses";
    }

    /**
     * User organizations page
     */
    @GetMapping("/{username}/organizations")
    public String profileOrganizationsGet(Model model,
                                          @PathVariable(value = "username") String username,
                                          HttpSession session, HttpServletRequest request) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        }
        Boolean[] ch = modelAddSettings(model, userEntity, user, request);
        if(!ch[2]) {
            return "redirect:/profile/" + userEntity.getUsername();
        }

        List<Organization> organizations = new ArrayList<>();

        for(Integer id : utilsService.splitToListInt(userEntity.getOrganizationsIds())) {
            OrganizationEntity organizationEntity = organizationRepository.findById(id).orElse(null);
            if(organizationEntity == null) continue;
            organizations.add(new Organization(organizationEntity));
        }

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity, request));
        model.addAttribute("organizations", organizations);

        return "profile/organizations";
    }

    /**
     * User settings page
     */
    @GetMapping("/{username}/settings")
    public String profileSettingsGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    HttpSession session, HttpServletRequest request) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();
        boolean isSelf = profile.getThird();

        if(userEntity == null || !isSelf) return "redirect:/profile";
        modelAddSettings(model, userEntity, user, request);

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity, request));

        return "profile/settings";
    }

    /**
     * User balance page
     * (only for your profile)
     */
    @GetMapping("/{username}/balance")
    public String profileBalanceGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    HttpSession session, HttpServletRequest request) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();
        boolean isSelf = profile.getThird();

        if(userEntity == null || !isSelf) return "redirect:/profile";
        modelAddSettings(model, userEntity, user, request);

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity, request));

        return "profile/balance";
    }

    /**
     * User follow button press
     */
    @PostMapping("/follow")
    public @ResponseBody ResponseEntity<Object> followBtn(@RequestParam(name = "username") String username,
                                                          HttpSession session, HttpServletRequest request) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);

        if(user.getUsername().equals(username) || userEntity == null)
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        friendsService.followPress(request, user.getId(), userEntity.getId());

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * User upload avatar
     */
    @PostMapping("/uploadAvatar")
    public @ResponseBody ResponseEntity<Object> uploadAvatar(@RequestParam(name = "username") String username,
                                                             @RequestBody String data,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(!user.getUsername().equals(username) || userEntity == null)
            return new ResponseEntity<>(HttpStatus.valueOf(500));

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

    @PostMapping("/{username}/settings")
    public String saveSettings(@ModelAttribute User user,
                               HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User sessionUser = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(sessionUser.getId()).orElse(null);

        if(userEntity == null) {
            return "redirect:/profile";
        }

        userEntity.setSettings(user.getSettings().toString());
        userRepository.save(userEntity);

        User cloneUser = new User(userEntity);
        session.setAttribute("user", cloneUser);

        return "redirect:/profile/" + userEntity.getUsername() + "/settings";
    }
}
