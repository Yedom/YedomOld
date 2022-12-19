package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.LanguageConfig;
import ru.mralexeimk.yedom.config.configs.ProfileConfig;
import ru.mralexeimk.yedom.database.entities.CompletedCoursesEntity;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.CompletedCoursesRepository;
import ru.mralexeimk.yedom.database.repositories.CoursesRepository;
import ru.mralexeimk.yedom.database.repositories.OrganizationsRepository;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.*;
import ru.mralexeimk.yedom.services.FriendsService;
import ru.mralexeimk.yedom.services.OrganizationsService;
import ru.mralexeimk.yedom.services.ProfileService;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.utils.custom.Triple;
import ru.mralexeimk.yedom.utils.custom.Pair;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller for user's profiles
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UtilsService utilsService;
    private final ProfileService profileService;
    private final FriendsService friendsService;
    private final UsersRepository usersRepository;
    private final OrganizationsRepository organizationsRepository;
    private final OrganizationsService organizationsService;
    private final CompletedCoursesRepository completedCoursesRepository;
    private final CoursesRepository coursesRepository;
    private final ProfileConfig profileConfig;
    private final LanguageConfig languageConfig;

    public ProfileController(UtilsService utilsService, ProfileService profileService, FriendsService friendsService, UsersRepository usersRepository, OrganizationsRepository organizationsRepository, OrganizationsService organizationsService, CompletedCoursesRepository completedCoursesRepository, CoursesRepository coursesRepository, ProfileConfig profileConfig, LanguageConfig languageConfig) {
        this.utilsService = utilsService;
        this.profileService = profileService;
        this.friendsService = friendsService;
        this.usersRepository = usersRepository;
        this.organizationsRepository = organizationsRepository;
        this.organizationsService = organizationsService;
        this.completedCoursesRepository = completedCoursesRepository;
        this.coursesRepository = coursesRepository;
        this.profileConfig = profileConfig;
        this.languageConfig = languageConfig;
    }

    /**
     * Get UserEntity of visited profile
     * @return UserEntity, User, Boolean (my profile or not)
     */
    private Triple<UserEntity, User, Boolean> getUserEntity(String username, HttpSession session) {
        User user = null;
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check == null) user = (User) session.getAttribute("user");

        UserEntity userEntity;
        boolean isSelf = false;

        // get my profile
        if(user != null && (username == null || username.equals(user.getUsername()))) {
            userEntity = usersRepository.findById(user.getId()).orElse(null);
            isSelf = true;
        }
        // get another user profile
        else {
            userEntity = usersRepository.findByUsername(username).orElse(null);
        }
        return new Triple<>(userEntity, user, isSelf);
    }

    /**
     * Get count of friends/followers/following/completed courses/organizations in Amounts class
     */
    private Amounts getAmounts(UserEntity userEntity) {
        Amounts amounts = new Amounts();
        amounts.setFriendsCount(friendsService.getCountOfFriends(userEntity.getId()));
        amounts.setFollowersCount(friendsService.getCountOfFollowers(userEntity.getId()));
        amounts.setFollowingCount(friendsService.getCountOfFollowing(userEntity.getId()));
        amounts.setCompletedCoursesCount(completedCoursesRepository.countAllByUserId(userEntity.getId()));
        amounts.setOrganizationsCount(organizationsService.getOrganizationsInCount(userEntity));

        return amounts;
    }

    private void modelGetSettings(Model model,
                                  UserEntity profileUser,
                                  User user) {
        modelGetSettings(model, profileUser, user, true);
    }

    /**
     * Get showing settings for profile
     */
    private Pair<Boolean[], String> modelGetSettings(Model model, UserEntity profileUser,
                                       User user, boolean add) {
        Boolean[] res = new Boolean[5];
        String relation = "strangers";
        if(user != null) {
            if(profileUser.getId() != user.getId()) {
                try {
                    if (friendsService.getFriendsGraph().
                            containsEdge(user.getId(), profileUser.getId())) {
                        relation = "friends";
                    }
                } catch (Exception ignored) {}
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
        res[4] = relation.equals("self")
                || (relation.equals("strangers") && settings.isStrangersShowOnline())
                || (relation.equals("friends") && settings.isFriendsShowOnline());

        var set = new Pair<>(res, relation);
        if(add) modelAddSettings(model, set);

        return set;
    }

    /**
     * Add showing settings to model
     */
    private void modelAddSettings(Model model, Pair<Boolean[], String> settings) {
        model.addAttribute("show_email", settings.getFirst()[0]);
        model.addAttribute("show_links", settings.getFirst()[1]);
        model.addAttribute("show_organizations", settings.getFirst()[2]);
        model.addAttribute("show_courses", settings.getFirst()[3]);
        model.addAttribute("show_online", settings.getFirst()[4]);
        model.addAttribute("relation", settings.getSecond());
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

    @GetMapping("/friends")
    public String indexFriends(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User user = (User) session.getAttribute("user");

        return "redirect:/profile/" + user.getUsername() + "/friends";
    }

    @GetMapping("/courses")
    public String indexCourses(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User user = (User) session.getAttribute("user");

        return "redirect:/profile/" + user.getUsername() + "/courses";
    }

    @GetMapping("/organizations")
    public String indexOrganizations(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User user = (User) session.getAttribute("user");

        return "redirect:/profile/" + user.getUsername() + "/organizations";
    }

    @GetMapping("/balance")
    public String indexBalance(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User user = (User) session.getAttribute("user");

        return "redirect:/profile/" + user.getUsername() + "/balance";
    }

    @GetMapping("/settings")
    public String indexSettings(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User user = (User) session.getAttribute("user");

        return "redirect:/profile/" + user.getUsername() + "/settings";
    }

    /**
     * Get profile page of user
     * (Authorize is not required)
     */
    @GetMapping("/{username}")
    public String profileGet(Model model,
                             @PathVariable(value = "username") String username,
                             HttpSession session) {
        Pair<String, String> pair = new Pair<>("profile", "profile");

        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        }

        modelGetSettings(model, userEntity, user);

        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity));

        return "profile/profile";
    }

    /**
     * User friends/followers/following pages
     */
    @GetMapping("/{username}/{type}")
    public String profileFriendsGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    @PathVariable(value = "type") String type,
                                    HttpSession session) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        }
        modelGetSettings(model, userEntity, user);

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity));

        switch (type) {
            case "friends" -> model.addAttribute("users",
                    friendsService.getFriendsList(userEntity.getId()));
            case "followers" -> model.addAttribute("users",
                    friendsService.getFollowersList(userEntity.getId()));
            case "following" -> model.addAttribute("users",
                    friendsService.getFollowingList(userEntity.getId()));
        }

        return "profile/friends";
    }

    /**
     * User completed courses page
     */
    @GetMapping("/{username}/courses")
    public String profileCoursesGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    HttpSession session) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        };
        var ch = modelGetSettings(model, userEntity, user, false);
        if(!ch.getFirst()[3]) {
            return "redirect:/profile/" + userEntity.getUsername();
        }
        modelAddSettings(model, ch);

        List<CompletedCoursesEntity> completedCoursesEntities =
                completedCoursesRepository.findAllByUserIdOrderByCompletedOnDesc(userEntity.getId());

        List<Course> completedCourses = new ArrayList<>();

        for(CompletedCoursesEntity completedCoursesEntity : completedCoursesEntities) {
            CourseEntity courseEntity = coursesRepository.findById(completedCoursesEntity.getCourseId()).orElse(null);
            if(courseEntity == null) continue;
            Course course = new Course(courseEntity);
            course.setCompletedOn(completedCoursesEntity.getCompletedOn());
            course.setTags(course.getTags().replaceAll("@", ", "));
            if(!course.isByOrganization()) {
                course.setCreatorName(Objects.requireNonNull(
                        usersRepository.findById(courseEntity.getCreatorId()).orElse(null)).getUsername());
            }
            else {
                course.setCreatorName(Objects.requireNonNull(
                        organizationsRepository.findById(courseEntity.getCreatorId()).orElse(null)).getName());
            }
            completedCourses.add(course);
        }

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity));

        model.addAttribute("completed_courses", completedCourses);

        return "profile/courses";
    }

    /**
     * User organizations page
     */
    @GetMapping("/{username}/organizations")
    public String profileOrganizationsGet(Model model,
                                          @PathVariable(value = "username") String username,
                                          HttpSession session) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();

        if(userEntity == null) {
            if(user != null) return "redirect:/auth/login";
            return "redirect:/";
        }
        var ch = modelGetSettings(model, userEntity, user, false);
        if(!ch.getFirst()[2]) {
            return "redirect:/profile/" + userEntity.getUsername();
        }
        modelAddSettings(model, ch);

        List<Organization> organizations = new ArrayList<>();

        for(Integer id : utilsService.splitToListInt(userEntity.getOrganizationsIds())) {
            OrganizationEntity organizationEntity = organizationsRepository.findById(id).orElse(null);
            if(organizationEntity == null) continue;
            organizations.add(new Organization(organizationEntity));
        }

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity));
        model.addAttribute("organizations", organizations);

        return "profile/organizations";
    }

    /**
     * User settings page
     * (only for your profile)
     */
    @GetMapping("/{username}/settings")
    public String profileSettingsGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    HttpSession session) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();
        boolean isSelf = profile.getThird();

        if(userEntity == null || !isSelf) return "redirect:/profile";
        modelGetSettings(model, userEntity, user);

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity));

        return "profile/settings";
    }

    /**
     * User balance page
     * (only for your profile)
     */
    @GetMapping("/{username}/balance")
    public String profileBalanceGet(Model model,
                                    @PathVariable(value = "username") String username,
                                    HttpSession session) {
        var profile = getUserEntity(username, session);
        UserEntity userEntity = profile.getFirst();
        User user = profile.getSecond();
        boolean isSelf = profile.getThird();

        if(userEntity == null || !isSelf) return "redirect:/profile";
        modelGetSettings(model, userEntity, user);

        if(user != null) model.addAttribute("session_username", user.getUsername());
        model.addAttribute("user", new User(userEntity));
        model.addAttribute("amounts",
                getAmounts(userEntity));

        return "profile/balance";
    }

    /**
     * User follow button press
     */
    @PostMapping("/follow")
    public @ResponseBody ResponseEntity<Object> followBtn(@RequestParam(name = "username") String username,
                                                          HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findByUsername(username).orElse(null);

        if(user.getUsername().equals(username) || userEntity == null)
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        friendsService.changeConnectionTypeBetweenUsers(userEntity.getId(), user.getId());

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Update user's links one by one
     */
    @PostMapping("/linksUpdate")
    public @ResponseBody ResponseEntity<Object> linksUpdate(@RequestBody String data,
                                                            HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);
            String links = json.getString("links");

            if (!profileService.isCorrectLinks(links)) return new ResponseEntity<>(HttpStatus.valueOf(500));

            userEntity.setLinks(links);
            usersRepository.save(userEntity);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Update user's 'about me' info
     */
    @PostMapping("/aboutUpdate")
    public @ResponseBody ResponseEntity<Object> aboutUpdate(@RequestBody String data,
                                                            HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findByUsername(user.getUsername()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);
            String about = json.getString("about");

            if (about.length() > 400) return new ResponseEntity<>(HttpStatus.valueOf(500));

            userEntity.setAbout(about);
            usersRepository.save(userEntity);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * User upload avatar
     */
    @PostMapping("/uploadAvatar")
    public @ResponseBody ResponseEntity<Object> uploadAvatar(@RequestBody String data,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null)
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);

            String baseImg = json.getString("baseImg");

            if(baseImg.length() > profileConfig.getBaseAvatarMaxSize() ||
                    !baseImg.split(",")[0].split("/")[0].equals("data:image")) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }

            userEntity.setAvatar(baseImg);
            usersRepository.save(userEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Post request to update user's settings
     */
    @PostMapping("/{username}/settings")
    public String saveSettings(@ModelAttribute User user,
                               HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        User sessionUser = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(sessionUser.getId()).orElse(null);

        if(userEntity == null) {
            return "redirect:/profile";
        }
        if(languageConfig.getLanguages().contains(user.getSettings().getLang())) {
            userEntity.setSettings(user.getSettings().toString());
        }
        usersRepository.save(userEntity);

        User cloneUser = new User(userEntity);
        session.setAttribute("user", cloneUser);

        return "redirect:/profile/" + userEntity.getUsername() + "/settings";
    }
}
