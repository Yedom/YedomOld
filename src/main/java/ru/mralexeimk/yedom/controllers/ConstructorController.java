package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.config.configs.DraftCoursesConfig;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.TagEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.DraftCourseRepository;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.database.repositories.TagRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.DraftCoursesService;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;
import ru.mralexeimk.yedom.utils.validators.DraftCourseValidator;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for courses constructor (user's draft courses)
 */
@Controller
@RequestMapping("/constructor")
public class ConstructorController {
    private final UtilsService utilsService;
    private final DraftCourseRepository draftCourseRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CoursesConfig coursesConfig;
    private final DraftCoursesConfig draftCoursesConfig;
    private final OrganizationsService organizationsService;
    private final DraftCoursesService draftCoursesService;
    private final DraftCourseValidator draftCourseValidator;
    private final TagRepository tagRepository;

    public ConstructorController(UtilsService utilsService, DraftCourseRepository draftCourseRepository, OrganizationRepository organizationRepository, UserRepository userRepository, CoursesConfig coursesConfig, DraftCoursesConfig draftCoursesConfig, OrganizationsService organizationsService, DraftCoursesService draftCoursesService, DraftCourseValidator draftCourseValidator, TagRepository tagRepository) {
        this.utilsService = utilsService;
        this.draftCourseRepository = draftCourseRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.coursesConfig = coursesConfig;
        this.draftCoursesConfig = draftCoursesConfig;
        this.organizationsService = organizationsService;
        this.draftCoursesService = draftCoursesService;
        this.draftCourseValidator = draftCourseValidator;
        this.tagRepository = tagRepository;
    }

    /**
     * Get draft courses list by user or organization
     */
    @GetMapping()
    public String index(Model model,
                        @RequestParam(value="section", defaultValue = "0") String sectionValue,
                        HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        List<DraftCourse> draftCourses = new ArrayList<>();

        if(userEntity == null) return "redirect:/";

        utilsService.addOptions(model, userEntity);

        try {
            // Get draft courses by user
            if (sectionValue.equals("0")) {
                for (Integer id : utilsService.splitToListInt(userEntity.getDraftCoursesIds())) {
                    DraftCourseEntity draftCourseEntity = draftCourseRepository.findById(id).orElse(null);
                    if (draftCourseEntity == null) continue;
                    draftCourses.add(new DraftCourse(draftCourseEntity));
                }
            }
            // Get draft courses by organization
            else {
                int orgId = Integer.parseInt(sectionValue);
                OrganizationEntity organizationEntity = organizationRepository.findById(orgId).orElse(null);
                if(organizationEntity != null && organizationsService.isMember(userEntity, orgId)) {
                    for (Integer id : utilsService.splitToListInt(organizationEntity.getDraftCoursesIds())) {
                        DraftCourseEntity draftCourseEntity = draftCourseRepository.findById(id).orElse(null);
                        if (draftCourseEntity == null) continue;
                        draftCourses.add(new DraftCourse(draftCourseEntity));
                    }
                }
            }
        } catch (Exception ignored) {}

        model.addAttribute("draft_courses", draftCourses);
        model.addAttribute("section_value", sectionValue);

        return "constructor/index";
    }

    /**
     * Open draft course constructor page
     */
    @GetMapping("/{hash}")
    public String draftCourse(Model model, @PathVariable String hash,
                              HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(draftCoursesService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("course", draftCourseEntity);

        String[] tags = draftCourseEntity.getTags().split("@");
        Integer[] tagsCountCourses = new Integer[tags.length];
        Arrays.fill(tagsCountCourses, 0);

        try {
            for (int i = 0; i < tags.length; ++i) {
                TagEntity tagEntity = tagRepository.findByTag(tags[i]).orElse(null);
                if (tagEntity != null) {
                    tagsCountCourses[i] = tagEntity.getCoursesCount();
                }
            }
        } catch (Exception ignored) {}

        model.addAttribute("tagsCountCourses", tagsCountCourses);

        return "constructor/draftcourse";
    }

    /**
     * Open draft courses edit page
     */
    @GetMapping("/{hash}/edit")
    public String draftCourseEdit(Model model, @PathVariable String hash,
                                      HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(draftCoursesService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("course", draftCourseEntity);

        return "constructor/edit";
    }

    /**
     * Open draft courses settings page
     */
    @GetMapping("/{hash}/settings")
    public String draftCourseSettings(Model model, @PathVariable String hash,
                              HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(draftCoursesService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("course", draftCourseEntity);

        return "constructor/settings";
    }

    /**
     * Open draft courses publication page
     */
    @GetMapping("/{hash}/public")
    public String draftCoursePublic(Model model, @PathVariable String hash,
                              HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(draftCoursesService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("course", draftCourseEntity);

        return "constructor/public";
    }

    /**
     * Post request to get recommended tags
     */
    @PostMapping("/{hash}/edit")
    public String saveSettings(@ModelAttribute("course") DraftCourseEntity draftCourseEntityChanges,
                               @PathVariable(value = "hash") String hash, BindingResult bindingResult,
                               HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(draftCoursesService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        draftCourseEntityChanges.setTags(utilsService.clearSpacesAroundSymbol(
                        draftCourseEntityChanges.getTags().replaceAll(", ", "@"), "@")
                .trim());

        draftCourseValidator.validate(new DraftCourse(draftCourseEntityChanges), bindingResult);

        if (bindingResult.hasErrors()) {
            return "constructor/edit";
        }

        draftCourseEntity.setTitle(draftCourseEntityChanges.getTitle());
        draftCourseEntity.setDescription(draftCourseEntityChanges.getDescription());
        draftCourseEntity.setTags(draftCourseEntityChanges.getTags());

        draftCourseRepository.save(draftCourseEntity);

        return "redirect:/constructor/" + hash + "/edit";
    }

    /**
     * Post request to delete draft course
     */
    @PostMapping("/{hash}/delete")
    public String courseDelete(@ModelAttribute("course") DraftCourseEntity draftCourseEntityChanges,
                               @PathVariable(value = "hash") String hash,
                               HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(draftCoursesService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        draftCourseRepository.delete(draftCourseEntity);

        return "redirect:/constructor/";
    }

    /**
     * User course avatar
     */
    @PostMapping("/{hash}/uploadAvatar")
    public @ResponseBody ResponseEntity<Object> uploadAvatar(@RequestBody String data,
                                                             @PathVariable String hash,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || draftCoursesService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);
            String baseImg = json.getString("baseImg");

            if(baseImg.length() > coursesConfig.getBaseAvatarMaxSize() ||
                    !baseImg.split(",")[0].split("/")[0].equals("data:image")) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }

            draftCourseEntity.setAvatar(baseImg);
            draftCourseRepository.save(draftCourseEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Get draft course modules or/and add new module
     */
    @GetMapping(value = "/{hash}/modules", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getCourseModules(@RequestParam(required = false, name = "del", defaultValue = "false") boolean del,
                                   @RequestParam(required = false, name = "module") String module,
                                   @RequestParam(required = false, name = "lesson") String lesson,
                                   @PathVariable String hash, HttpSession session) {
        StringBuilder htmlResponse = new StringBuilder();
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;
        String bad = utilsService.jsonToString(htmlResponse, "modules");

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return bad;

        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || draftCoursesService.hasNoAccess(userEntity, draftCourseEntity))
            return bad;

        var modules = draftCoursesService.getModulesFromString(draftCourseEntity.getModules());
        if(module != null && !module.isEmpty()) {
            if(!del && module.length() > draftCoursesConfig.getMaxModuleAndLessonNameLength() ||
                    utilsService.containsSymbols(module, draftCoursesConfig.getModulesDisabledSymbols())) {
                return bad;
            }
            List<String> lessons = modules.getOrDefault(module, new ArrayList<>());
            if (lesson != null && !lesson.isEmpty()) {
                if(!del && lesson.length() > draftCoursesConfig.getMaxModuleAndLessonNameLength() ||
                        utilsService.containsSymbols(lesson, draftCoursesConfig.getModulesDisabledSymbols())) {
                    return bad;
                }
                if(!del) {
                    lessons.add(lesson);
                    if (lessons.size() >= draftCoursesConfig.getMaxLessons())
                        return bad;
                }
                else {
                    lessons.remove(lesson);
                }
            }
            else {
                if(del) {
                    modules.remove(module);
                }
            }
            if(!del) {
                modules.put(module, lessons);
                if (modules.size() >= draftCoursesConfig.getMaxModules())
                    return bad;
            }
        }

        for(String key : modules.keySet()) {
            htmlResponse.append("<button type='button' class='collapsible' style='width: 80%'>");
            htmlResponse.append(key);
            htmlResponse.append("</button>&nbsp;");
            htmlResponse.append("<i onclick='delModule(\"").append(key).append("\")' class='material-icons centered-icon delete-icon'>delete</i>");
            htmlResponse.append("<div class='content-collapse'>");
            for(String lessonName : modules.get(key)) {
                htmlResponse.append("* <button id='").append(lessonName).
                        append("' ").append("onclick='loadLesson('").
                        append(key).append("':").append(lessonName).
                        append(")' type='button'>");
                htmlResponse.append(lessonName);
                htmlResponse.append("</button>");
            }
            htmlResponse.append("<button id='addLesson' onclick='addLesson()' type='button' class='collapsible'>");
            htmlResponse.append("<i class='material-icons add-icon'>add</i>");
            htmlResponse.append("</button>");
            htmlResponse.append("</div>");
        }

        draftCourseEntity.setModules(draftCoursesService.getModulesFromMap(modules));
        draftCourseRepository.save(draftCourseEntity);

        return utilsService.jsonToString(htmlResponse, "modules");
    }
}
