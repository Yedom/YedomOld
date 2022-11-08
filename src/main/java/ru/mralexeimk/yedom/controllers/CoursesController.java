package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.entities.TagEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.enums.SocketType;
import ru.mralexeimk.yedom.database.repositories.CourseRepository;
import ru.mralexeimk.yedom.database.repositories.TagRepository;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.services.RolesService;
import ru.mralexeimk.yedom.utils.services.TagsService;
import ru.mralexeimk.yedom.utils.validators.CourseValidator;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/courses")
public class CoursesController {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseValidator courseValidator;
    private final TagRepository tagRepository;
    private final TagsService tagsService;
    private final RolesService rolesService;
    private final LanguageUtil languageUtil;

    @Autowired
    public CoursesController(CourseRepository courseRepository, UserRepository userRepository, CourseValidator courseValidator, TagRepository tagRepository, TagsService tagsService, RolesService rolesService, LanguageUtil languageUtil) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseValidator = courseValidator;
        this.tagRepository = tagRepository;
        this.tagsService = tagsService;
        this.rolesService = rolesService;
        this.languageUtil = languageUtil;
    }

    @GetMapping
    public String index(Model model, @RequestParam(required = false, name = "search") String search,
                        HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        List<Course> courses = new ArrayList<>();
        if(search == null) {
            courses = courseRepository.findByOrderByViewsDesc().stream()
                    .map(Course::new)
                    .peek(course -> {
                        if(!course.isByOrganization()) {
                            Optional<UserEntity> userEntity = userRepository.findById(course.getCreatorId());
                            userEntity.ifPresent(entity -> course.setCreatorName(userEntity.get().getUsername()));
                        }
                    })
                    .toList();
        }
        else {
            String response = tagsService.sendSocket(user, SocketType.SEARCH_COURSES, search);
            if(!response.equals("")) {
                List<Integer> IDS = tagsService.responseIdsToList(response);

                for (Integer id : IDS) {
                    CourseEntity courseEntity = courseRepository.findById(id).orElse(null);
                    if(courseEntity != null) {
                        Course course = new Course(courseEntity);
                        if(!course.isByOrganization()) {
                            Optional<UserEntity> userEntity = userRepository.findById(course.getCreatorId());
                            userEntity.ifPresent(entity -> course.setCreatorName(userEntity.get().getUsername()));
                        }
                        courses.add(course);
                    }
                }
            }
        }

        model.addAttribute("courses", courses);

        return "courses/index";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("course") Course course, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;
        return "courses/add";
    }

    @PostMapping("/add")
    public String addPost(@Valid @ModelAttribute("course") Course course,
                          BindingResult bindingResult, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        if(!rolesService.hasPermission(user, "courses.add")) {
            bindingResult.rejectValue("title",
                    "common.permission",
                    languageUtil.getLocalizedMessage("common.permission"));
            return "courses/add";
        }

        course.setByOrganization(false);
        course.setCreatorId(user.getId());
        CourseEntity courseEntity = new CourseEntity(course);
        courseRepository.save(courseEntity);

        return "redirect:/courses";
    }

    @GetMapping(value = "/add/tagsUpdate", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String tagsUpdate(@RequestParam(required = false, name = "tags") String tags,
                             HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        StringBuilder htmlResponse = new StringBuilder();
        if(tags != null && !tags.equals("")) {
            try {
                User user = (User) session.getAttribute("user");
                Set<String> tagsSet = new HashSet<>(Arrays.asList(tags.split("@")[0].split(", ")));
                tags = tags
                        .replaceAll(",", "")
                        .replaceAll("@", " ");
                String search = CommonUtils.getLastN(
                        tags.split(" "),
                        YedomConfig.MAX_WORDS_IN_REQUEST);
                String response = tagsService.sendSocket(user, SocketType.SEARCH_RELATED_TAGS, search.strip());

                if (!response.equals("")) {
                    List<Integer> IDS = tagsService.responseIdsToList(response);

                    int c = 0;
                    for (int id : IDS) {
                        TagEntity tagEntity = tagRepository.findById(id).orElse(null);
                        if (c > YedomConfig.MAX_TAGS_SUGGESTIONS) break;
                        if (tagEntity == null || tagsSet.contains(tagEntity.getTag())) continue;
                        htmlResponse
                                .append("<span onclick=\"spanClick(this)\">")
                                .append(tagEntity.getTag())
                                .append("</span>");
                        ++c;
                    }
                }
            } catch (Exception ignored) {}
        }
        return new JSONObject(Map.of("tags", htmlResponse.toString())).toString();
    }

    @GetMapping(value = "/popularTags", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String popularTags(HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        StringBuilder htmlResponse = new StringBuilder();
        User user = (User) session.getAttribute("user");
        String response = tagsService.sendSocket(user, SocketType.GET_POPULAR_TAGS);

        if (!response.equals("")) {
            List<Integer> IDS = tagsService.responseIdsToList(response);

            int c = 0;
            for (int id : IDS) {
                TagEntity tagEntity = tagRepository.findById(id).orElse(null);
                if (c > YedomConfig.MAX_TAGS_SUGGESTIONS) break;
                if (tagEntity == null) continue;
                htmlResponse
                        .append("<span class=\"tag\" onclick=\"spanClick(this)\">")
                        .append(tagEntity.getTag())
                        .append("</span>");
                ++c;
            }
        }
        return new JSONObject(Map.of("tags", htmlResponse.toString())).toString();
    }
}
