package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.config.configs.DraftCoursesConfig;
import ru.mralexeimk.yedom.config.configs.SmartSearchConfig;
import ru.mralexeimk.yedom.database.entities.*;
import ru.mralexeimk.yedom.database.repositories.*;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.utils.enums.HashAlg;
import ru.mralexeimk.yedom.utils.enums.SocketType;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;
import ru.mralexeimk.yedom.utils.services.RolesService;
import ru.mralexeimk.yedom.utils.services.TagsService;
import ru.mralexeimk.yedom.utils.validators.DraftCourseValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/courses")
public class CoursesController {
    private final UtilsService utilsService;
    private final CourseRepository courseRepository;
    private final DraftCourseRepository draftCourseRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DraftCourseValidator draftCourseValidator;
    private final TagRepository tagRepository;
    private final TagsService tagsService;
    private final OrganizationsService organizationsService;
    private final SmartSearchConfig smartSearchConfig;
    private final CoursesConfig coursesConfig;
    private final DraftCoursesConfig draftCoursesConfig;

    @Autowired
    public CoursesController(UtilsService utilsService, CourseRepository courseRepository, DraftCourseRepository draftCourseRepository, OrganizationRepository organizationRepository, UserRepository userRepository, DraftCourseValidator draftCourseValidator, TagRepository tagRepository, TagsService tagsService, OrganizationsService organizationsService, SmartSearchConfig smartSearchConfig, CoursesConfig coursesConfig, DraftCoursesConfig draftCoursesConfig) {
        this.utilsService = utilsService;
        this.courseRepository = courseRepository;
        this.draftCourseRepository = draftCourseRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.draftCourseValidator = draftCourseValidator;
        this.tagRepository = tagRepository;
        this.tagsService = tagsService;
        this.draftCoursesConfig = draftCoursesConfig;
        this.organizationsService = organizationsService;
        this.smartSearchConfig = smartSearchConfig;
        this.coursesConfig = coursesConfig;
    }

    /**
     * Model's course set creatorName and avatar
     */
    private void courseProcess(Course course) {
        if(!course.isByOrganization()) {
            Optional<UserEntity> userEntity = userRepository.findById(course.getCreatorId());
            userEntity.ifPresent(entity -> {
                course.setCreatorName(userEntity.get().getUsername());
                course.setCreatorAvatar(userEntity.get().getAvatar());
                course.setCreatorType("profile");
            });
        }
        else {
            Optional<OrganizationEntity> organizationEntity = organizationRepository.findById(course.getCreatorId());
            organizationEntity.ifPresent(entity -> {
                course.setCreatorName(organizationEntity.get().getName());
                course.setCreatorAvatar(organizationEntity.get().getAvatar());
                course.setCreatorType("organization");
            });
        }
    }

    /**
     * Open courses list page
     * @param search user search query
     */
    @GetMapping
    public String index(Model model,
                        @RequestParam(required = false, name = "search") String search,
                        @RequestParam(required = false, name = "tag") String tag,
                        HttpServletRequest request) {
        List<Course> courses = new ArrayList<>();
        if(search == null) {
            courses = courseRepository.findByOrderByViewsDesc().stream()
                    .map(Course::new)
                    .peek(this::courseProcess)
                    .toList();
        }
        else {
            String response;
            if(tag == null) {
                response = tagsService.sendSocket(
                        request.getSession().getId(), SocketType.SEARCH_COURSES, search);
            }
            else {
                response = tagsService.sendSocket(
                        request.getSession().getId(), SocketType.SEARCH_COURSES_TAG, search);
            }

            if(!response.equals("")) {
                List<Integer> IDS = utilsService.splitToListInt(response);

                for (Integer id : IDS) {
                    CourseEntity courseEntity = courseRepository.findById(id).orElse(null);
                    if(courseEntity != null) {
                        Course course = new Course(courseEntity);
                        courseProcess(course);
                        courses.add(course);
                    }
                }
            }
        }

        model.addAttribute("courses", courses);

        return "courses/index";
    }

    /**
     * Get course page by hash
     */
    @GetMapping("/{hash}")
    public String course(Model model, @PathVariable String hash) {
        CourseEntity courseEntity = courseRepository.findByHash(hash).orElse(null);
        if(courseEntity == null) return "redirect:/courses";

        model.addAttribute("course", new Course(courseEntity));

        return "courses/course";
    }

    /**
     * Open create course page
     */
    @GetMapping("/add")
    public String add(Model model,
                      @ModelAttribute("course") Course course,
                      HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        if(userEntity == null) return "redirect:/courses";

        utilsService.addOptions(model, userEntity);

        return "courses/add";
    }

    /**
     * Save course in user's draft courses
     */
    @PostMapping("/add")
    public String addPost(Model model,
                          @ModelAttribute("course") DraftCourse draftCourse,
                          @RequestParam(value="section", defaultValue = "0") String sectionValue,
                          BindingResult bindingResult, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/courses";

        utilsService.addOptions(model, userEntity);

        DraftCourse cloneCourse = new DraftCourse(draftCourse);
        cloneCourse.setTags(
                utilsService.clearSpacesAroundSymbol(
                                cloneCourse.getTags().replaceAll(", ", "@"), "@")
                        .trim());
        draftCourseValidator.validate(cloneCourse, bindingResult);

        if(bindingResult.hasErrors()) {
            return "courses/add";
        }

        try {
            DraftCourseEntity courseEntity = new DraftCourseEntity(cloneCourse);
            courseEntity.setAvatar(coursesConfig.getBaseAvatarDefault());
            courseEntity.setAddedOn(utilsService.getCurrentTimestamp());
            if(draftCourseRepository.isNotEmpty())
                courseEntity.setHash(utilsService.hash(draftCourseRepository.getLastId() + 1,
                        HashAlg.SHA256));
            else
                courseEntity.setHash(utilsService.hash(1, HashAlg.SHA256));

            if(sectionValue.equals("0")) {
                if(draftCourseRepository.countByCreatorId(userEntity.getId()) >=
                        draftCoursesConfig.getMaxPerUser()) {
                    utilsService.reject("name", "courses.limit", bindingResult);
                    return "courses/add";
                }
                courseEntity.setByOrganization(false);
                courseEntity.setCreatorId(userEntity.getId());
                draftCourseRepository.save(courseEntity);
                List<String> draftCoursesIds = utilsService.splitToListString(userEntity.getDraftCoursesIds());
                draftCoursesIds.add(String.valueOf(courseEntity.getId()));
                userEntity.setDraftCoursesIds(utilsService.listToString(draftCoursesIds));
                userRepository.save(userEntity);
            }
            else {
                int orgId = Integer.parseInt(sectionValue);
                OrganizationEntity organizationEntity = organizationRepository.findById(orgId).orElse(null);
                if(organizationEntity != null && organizationsService.isMember(userEntity, orgId)) {
                    if(draftCourseRepository.countByCreatorId(orgId) >=
                            draftCoursesConfig.getMaxPerOrganization()) {
                        utilsService.reject("name", "courses.limit", bindingResult);
                        return "courses/add";
                    }
                    courseEntity.setByOrganization(true);
                    courseEntity.setCreatorId(orgId);
                    draftCourseRepository.save(courseEntity);
                    List<String> draftCoursesIds = utilsService.splitToListString(organizationEntity.getDraftCoursesIds());
                    draftCoursesIds.add(String.valueOf(courseEntity.getId()));
                    organizationEntity.setDraftCoursesIds(utilsService.listToString(draftCoursesIds));
                    organizationRepository.save(organizationEntity);
                }
                else throw new Exception();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            utilsService.reject("title", "common.error", bindingResult);
            return "courses/add";
        }

        return "redirect:/constructor";
    }

    /**
     * Update tags recommendations when user typing
     */
    @GetMapping(value = "/add/tagsUpdate", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String tagsUpdate(@RequestParam(required = false, name = "tags") String tags,
                                HttpServletRequest request, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        StringBuilder htmlResponse = new StringBuilder();
        if(tags != null && !tags.equals("")) {
            try {
                User user = (User) session.getAttribute("user");
                Set<String> tagsSet = new HashSet<>(Arrays.asList(tags.split("@")[0].split(", ")));
                tags = tags
                        .replaceAll(",", "")
                        .replaceAll("@", " ");
                String search = utilsService.getLastN(
                        tags.split(" "),
                        smartSearchConfig.getMaxWordsInRequest());
                String response = tagsService.sendSocket(
                        request.getSession().getId(), SocketType.SEARCH_RELATED_TAGS, search.strip());

                if (!response.equals("")) {
                    List<Integer> IDS = utilsService.splitToListInt(response);

                    int c = 0;
                    for (int id : IDS) {
                        TagEntity tagEntity = tagRepository.findById(id).orElse(null);
                        if (c > smartSearchConfig.getMaxTagsSuggestions()) break;
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
        return utilsService.jsonToString(htmlResponse, "tags");
    }

    /**
     * Get popular tags
     */
    @GetMapping(value = "/popularTags", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String popularTags(HttpServletRequest request) {
        StringBuilder htmlResponse = new StringBuilder();
        String response = tagsService.sendSocket(
                request.getSession().getId(), SocketType.GET_POPULAR_TAGS);

        if (!response.equals("")) {
            List<Integer> IDS = utilsService.splitToListInt(response);

            int c = 0;
            for (int id : IDS) {
                TagEntity tagEntity = tagRepository.findById(id).orElse(null);
                if (c > smartSearchConfig.getMaxTagsSuggestions()) break;
                if (tagEntity == null) continue;
                htmlResponse
                        .append("<span class=\"tag\" onclick=\"spanClick(this)\">")
                        .append(tagEntity.getTag())
                        .append("</span>");
                ++c;
            }
        }
        return utilsService.jsonToString(htmlResponse, "tags");
    }
}
