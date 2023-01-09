package ru.mralexeimk.yedom.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.mralexeimk.yedom.configs.properties.CoursesConfig;
import ru.mralexeimk.yedom.configs.properties.ConstructorConfig;
import ru.mralexeimk.yedom.configs.properties.SmartSearchConfig;
import ru.mralexeimk.yedom.database.entities.*;
import ru.mralexeimk.yedom.database.repositories.*;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.models.Module;
import ru.mralexeimk.yedom.services.*;
import ru.mralexeimk.yedom.utils.enums.HashAlg;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.validators.ConstructorValidator;

import java.util.*;

/**
 * Controller for courses list/search/add
 */
@Controller
@RequestMapping("/courses")
public class CoursesController {
    private final UtilsService utilsService;
    private final CoursesService coursesService;
    private final ValidationService validationService;
    private final CoursesRepository coursesRepository;
    private final DraftCoursesRepository draftCoursesRepository;
    private final OrganizationsRepository organizationsRepository;
    private final UsersRepository usersRepository;
    private final ConstructorValidator constructorValidator;
    private final TagsService tagsService;
    private final OrganizationsService organizationsService;
    private final SmartSearchConfig smartSearchConfig;
    private final CoursesConfig coursesConfig;
    private final ConstructorConfig constructorConfig;

    @Autowired
    public CoursesController(UtilsService utilsService, CoursesService coursesService, ValidationService validationService, CoursesRepository coursesRepository, DraftCoursesRepository draftCoursesRepository, OrganizationsRepository organizationsRepository, UsersRepository usersRepository, ConstructorValidator constructorValidator, TagsService tagsService, OrganizationsService organizationsService, SmartSearchConfig smartSearchConfig, CoursesConfig coursesConfig, ConstructorConfig constructorConfig) {
        this.utilsService = utilsService;
        this.coursesService = coursesService;
        this.validationService = validationService;
        this.coursesRepository = coursesRepository;
        this.draftCoursesRepository = draftCoursesRepository;
        this.organizationsRepository = organizationsRepository;
        this.usersRepository = usersRepository;
        this.constructorValidator = constructorValidator;
        this.tagsService = tagsService;
        this.constructorConfig = constructorConfig;
        this.organizationsService = organizationsService;
        this.smartSearchConfig = smartSearchConfig;
        this.coursesConfig = coursesConfig;
    }

    /**
     * Model's course set creatorName and avatar
     */
    private void courseProcess(Course course) {
        if(!course.isByOrganization()) {
            Optional<UserEntity> userEntity = usersRepository.findById(course.getCreatorId());
            userEntity.ifPresent(entity -> {
                course.setCreatorName(userEntity.get().getUsername());
                course.setCreatorAvatar(userEntity.get().getAvatar());
                course.setCreatorType("profile");
            });
        }
        else {
            Optional<OrganizationEntity> organizationEntity = organizationsRepository.findById(course.getCreatorId());
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
                        @RequestParam(required = false, name = "tag") String tag) {
        List<Course> courses;
        if(search == null) {
            Set<CourseEntity> popularCourses = new HashSet<>();
            tagsService.getPopularTags().forEach(
                    tagTitle -> popularCourses.addAll(tagsService.searchCoursesByTag(tagTitle)));
            courses = popularCourses.stream()
                    .map(Course::new)
                    .peek(this::courseProcess)
                    .toList();
        }
        else {
            if(tag == null) {
                courses = tagsService.searchCoursesByInput(search).stream()
                        .map(Course::new)
                        .peek(this::courseProcess)
                        .toList();
            }
            else {
                courses = tagsService.searchCoursesByTag(search).stream()
                        .map(Course::new)
                        .peek(this::courseProcess)
                        .toList();
            }
        }

        model.addAttribute("courses", courses);

        return "courses/courses";
    }

    /**
     * Get course page by hash
     */
    @GetMapping("/{hash}")
    public String course(Model model,
                         @RequestParam(required = false, name = "active") String activeModules,
                         @PathVariable String hash) {
        CourseEntity courseEntity = coursesRepository.findByHash(hash).orElse(null);
        if(courseEntity == null) return "redirect:/courses";

        model.addAttribute("course", courseEntity);
        coursesService.generateActiveModules(model, activeModules);

        String[] tags = courseEntity.getTags().split("@");
        Integer[] tagsCountCourses = new Integer[tags.length];
        Arrays.fill(tagsCountCourses, 0);

        for (int i = 0; i < tags.length; ++i) {
            try {
                tagsCountCourses[i] = tagsService.getGraphInfo().get(tags[i]).getCourses().size();
            } catch (Exception ignored) {}
        }

        model.addAttribute("tagsCountCourses", tagsCountCourses);

        return "courses/course";
    }

    /**
     * Get lesson page
     */
    @GetMapping(value = "/{hash}/{moduleId}/{lessonId}")
    public String getLesson(Model model, @PathVariable String hash,
                            @RequestParam(required = false, name = "active") String activeModules,
                            @PathVariable String moduleId, @PathVariable String lessonId) {
        CourseEntity courseEntity = coursesRepository.findByHash(hash).orElse(null);
        if(courseEntity == null) return "redirect:/courses";

        model.addAttribute("course", courseEntity);
        coursesService.generateActiveModules(model, activeModules);

        try {
            int mid = Integer.parseInt(moduleId);
            int lid = Integer.parseInt(lessonId);

            LinkedList<Module> modules =
                    coursesService.getModulesFromString(courseEntity.getModules());
            if(mid >= modules.size()) throw new Exception();
            if(lid >= modules.get(mid).getLessons().size()) throw new Exception();

            model.addAttribute("moduleId", mid);
            model.addAttribute("lessonId", lid);
        } catch (Exception ex) {
            return "redirect:/courses/" + hash;
        }

        return "courses/lesson";
    }

    /**
     * Get video of lesson
     */
    @GetMapping(value = "/{hash}/{moduleId}/{lessonId}/video")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getVideo(@PathVariable String hash,
                                                          @PathVariable String moduleId, @PathVariable String lessonId) {
        CourseEntity courseEntity = coursesRepository.findByHash(hash).orElse(null);
        if(courseEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            int mid = Integer.parseInt(moduleId);
            int lid = Integer.parseInt(lessonId);

            LinkedList<Module> modules =
                    coursesService.getModulesFromString(courseEntity.getModules());
            if(mid >= modules.size()) throw new Exception();
            if(lid >= modules.get(mid).getLessons().size()) throw new Exception();
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return coursesService.getVideoHtmlContent(
                coursesConfig.getVideosPath() + hash + "/" + moduleId + "/" + lessonId + ".mp4");
    }

    /**
     * Get modules by course hash
     */
    @GetMapping(value = "/{hash}/modules")
    public String getCourseModules(Model model, @PathVariable String hash) {
        CourseEntity courseEntity = coursesRepository.findByHash(hash).orElse(null);
        if(courseEntity == null) return "redirect:/courses";

        model.addAttribute("modules",
                coursesService.getModulesFromString(courseEntity.getModules()));

        return "courses/modules";
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
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        if(userEntity == null) return "redirect:/courses";

        coursesService.generateOptions(model, userEntity);

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
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/courses";

        coursesService.generateOptions(model, userEntity);

        DraftCourse cloneCourse = new DraftCourse(draftCourse);
        cloneCourse.setTags(
                utilsService.clearSpacesAroundSymbol(
                                cloneCourse.getTags().replaceAll(", ", "@"), "@")
                        .trim());
        constructorValidator.validate(cloneCourse, bindingResult);

        if(bindingResult.hasErrors()) {
            return "courses/add";
        }

        try {
            DraftCourseEntity courseEntity = new DraftCourseEntity(cloneCourse);
            courseEntity.setAvatar(coursesConfig.getBaseAvatarDefault());
            courseEntity.setAddedOn(utilsService.getCurrentTimestamp());
            if(draftCoursesRepository.isNotEmpty())
                courseEntity.setHash(utilsService.hash(draftCoursesRepository.getLastId() + 1,
                        HashAlg.SHA256));
            else
                courseEntity.setHash(utilsService.hash(1, HashAlg.SHA256));

            if(sectionValue.equals("0")) {
                if(draftCoursesRepository.countByCreatorId(userEntity.getId()) >=
                        constructorConfig.getMaxPerUser()) {
                    validationService.reject("name", "courses.limit", bindingResult);
                    return "courses/add";
                }
                courseEntity.setByOrganization(false);
                courseEntity.setCreatorId(userEntity.getId());
                draftCoursesRepository.save(courseEntity);
                List<String> draftCoursesIds = utilsService.splitToListString(userEntity.getDraftCoursesIds());
                draftCoursesIds.add(String.valueOf(courseEntity.getId()));
                userEntity.setDraftCoursesIds(String.join(",", draftCoursesIds));
                usersRepository.save(userEntity);
            }
            else {
                int orgId = Integer.parseInt(sectionValue);
                OrganizationEntity organizationEntity = organizationsRepository.findById(orgId).orElse(null);
                if(organizationEntity != null && organizationsService.isMember(userEntity, orgId)) {
                    if(draftCoursesRepository.countByCreatorId(orgId) >=
                            constructorConfig.getMaxPerOrganization()) {
                        validationService.reject("name", "courses.limit", bindingResult);
                        return "courses/add";
                    }
                    courseEntity.setByOrganization(true);
                    courseEntity.setCreatorId(orgId);
                    draftCoursesRepository.save(courseEntity);
                    List<String> draftCoursesIds = utilsService.splitToListString(organizationEntity.getDraftCoursesIds());
                    draftCoursesIds.add(String.valueOf(courseEntity.getId()));
                    organizationEntity.setDraftCoursesIds(String.join(",", draftCoursesIds));
                    organizationsRepository.save(organizationEntity);
                }
                else throw new Exception();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            validationService.reject("title", "common.error", bindingResult);
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
                                HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        StringBuilder htmlResponse = new StringBuilder();
        if(tags == null || tags.equals(""))
            return utilsService.jsonToString(htmlResponse, "tags");

        try {
            Set<String> tagsSet = new HashSet<>(Arrays.asList(tags.split("@")[0].split(", ")));
            tags = tags
                    .replaceAll(",", "")
                    .replaceAll("@", " ");
            String search = utilsService.joinLastN(
                    tags.split(" "),
                    smartSearchConfig.getMaxWordsInRequest()).strip();
            Set<String> relatedTags = tagsService.searchRelatedTags(search);
            relatedTags.removeAll(tagsSet);

            int c = 0;
            for(String tag : relatedTags) {
                if (c > smartSearchConfig.getMaxTagsSuggestions()) break;
                htmlResponse
                        .append("<span onclick=\"spanClick(this)\">")
                        .append(tag)
                        .append("</span>");
                ++c;
            }
        } catch (Exception ignored) {}

        return utilsService.jsonToString(htmlResponse, "tags");
    }

    /**
     * Get popular tags
     */
    @GetMapping(value = "/popularTags", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String popularTags() {
        StringBuilder htmlResponse = new StringBuilder();
        List<String> popularTags = tagsService.getPopularTags();

        int c = 0;
        for(String tag : popularTags) {
            if (c > smartSearchConfig.getMaxTagsSuggestions()) break;
            htmlResponse
                    .append("<span class=\"tag\" onclick=\"spanClick(this)\">")
                    .append(tag)
                    .append("</span>");
            ++c;
        }

        return utilsService.jsonToString(htmlResponse, "tags");
    }
}
