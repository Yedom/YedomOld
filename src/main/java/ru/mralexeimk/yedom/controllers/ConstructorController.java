package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.mralexeimk.yedom.config.configs.ConstructorConfig;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.database.entities.*;
import ru.mralexeimk.yedom.database.repositories.*;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.models.Module;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.services.*;
import ru.mralexeimk.yedom.utils.validators.ConstructorValidator;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

/**
 * Controller for courses constructor (user's draft courses)
 */
@Controller
@RequestMapping("/constructor")
public class ConstructorController {
    private final UtilsService utilsService;
    private final CoursesService coursesService;
    private final DraftCoursesRepository draftCoursesRepository;
    private final UsersRepository usersRepository;
    private final CoursesConfig coursesConfig;
    private final ConstructorService constructorService;
    private final ModerationService moderationService;
    private final RolesService rolesService;
    private final LoaderService loaderService;
    private final TagsService tagsService;
    private final ConstructorValidator constructorValidator;
    private final ConstructorConfig constructorConfig;

    public ConstructorController(UtilsService utilsService, CoursesService coursesService, DraftCoursesRepository draftCoursesRepository, UsersRepository usersRepository, CoursesConfig coursesConfig, ConstructorService constructorService, ModerationService moderationService, RolesService rolesService, LoaderService loaderService, TagsService tagsService, ConstructorValidator constructorValidator, ConstructorConfig constructorConfig) {
        this.utilsService = utilsService;
        this.coursesService = coursesService;
        this.draftCoursesRepository = draftCoursesRepository;
        this.usersRepository = usersRepository;
        this.coursesConfig = coursesConfig;
        this.constructorService = constructorService;
        this.moderationService = moderationService;
        this.rolesService = rolesService;
        this.loaderService = loaderService;
        this.tagsService = tagsService;
        this.constructorValidator = constructorValidator;
        this.constructorConfig = constructorConfig;
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
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/";

        coursesService.generateOptions(model, userEntity);
        coursesService.generateDraftCourses(model, userEntity, sectionValue);

        model.addAttribute("section_value", sectionValue);

        return "constructor/constructor";
    }

    /**
     * Open draft course constructor page
     */
    @GetMapping("/{hash}")
    public String draftCourse(Model model,
                              @RequestParam(required = false, name = "active") String activeModules,
                              @PathVariable String hash,
                              HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("course", draftCourseEntity);

        String[] tags = draftCourseEntity.getTags().split("@");
        Integer[] tagsCountCourses = new Integer[tags.length];
        Arrays.fill(tagsCountCourses, 0);

        for (int i = 0; i < tags.length; ++i) {
            try {
                tagsCountCourses[i] = tagsService.getGraphInfo().get(tags[i]).getCourses().size();
            } catch (Exception ignored) {}
        }

        constructorService.addActiveModules(model, activeModules, draftCourseEntity);

        model.addAttribute("tagsCountCourses", tagsCountCourses);

        return "constructor/draftcourse";
    }

    /**
     * Open draft courses edit page
     */
    @GetMapping("/{hash}/edit")
    public String draftCourseEdit(Model model,
                                  @RequestParam(required = false, name = "active") String activeModules,
                                  @PathVariable String hash,
                                      HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        constructorService.addActiveModules(model, activeModules, draftCourseEntity);

        model.addAttribute("course", draftCourseEntity);

        return "constructor/edit";
    }

    /**
     * Open draft courses settings page
     */
    @GetMapping("/{hash}/settings")
    public String draftCourseSettings(Model model,
                                      @RequestParam(required = false, name = "active") String activeModules,
                                      @PathVariable String hash,
                                        HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        constructorService.addActiveModules(model, activeModules, draftCourseEntity);

        model.addAttribute("course", draftCourseEntity);

        return "constructor/settings";
    }

    /**
     * Open draft courses publication page
     */
    @GetMapping("/{hash}/public")
    public String draftCoursePublic(Model model,
                                    @RequestParam(required = false, name = "active") String activeModules,
                                    @PathVariable String hash,
                                    HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        constructorService.addActiveModules(model, activeModules, draftCourseEntity);

        model.addAttribute("course", draftCourseEntity);

        return "constructor/public";
    }

    /**
     * Send draft course on moderation
     */
    @PostMapping("/{hash}/public")
    public String sendToModeration(@PathVariable String hash,
                                   HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        draftCourseEntity.setPublicRequest(!draftCourseEntity.isPublicRequest());
        draftCoursesRepository.save(draftCourseEntity);

        moderationService.update(draftCourseEntity);

        return "redirect:/constructor/" + hash + "/public";
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
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        draftCourseEntityChanges.setTags(utilsService.clearSpacesAroundSymbol(
                        draftCourseEntityChanges.getTags().replaceAll(", ", "@"), "@")
                .trim());

        constructorValidator.validate(new DraftCourse(draftCourseEntityChanges), bindingResult);

        if (bindingResult.hasErrors()) {
            return "constructor/edit";
        }

        draftCourseEntity.setTitle(draftCourseEntityChanges.getTitle());
        draftCourseEntity.setDescription(draftCourseEntityChanges.getDescription());
        draftCourseEntity.setTags(draftCourseEntityChanges.getTags());

        draftCoursesRepository.save(draftCourseEntity);

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
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(constructorService.hasNoAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        constructorService.removeCourse(hash);
        draftCoursesRepository.delete(draftCourseEntity);

        return "redirect:/constructor/";
    }

    /**
     * Upload course avatar
     */
    @PostMapping("/{hash}/uploadAvatar")
    public @ResponseBody ResponseEntity<Object> uploadAvatar(@RequestBody String data,
                                                             @PathVariable String hash,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);
            String baseImg = json.getString("baseImg");

            if(baseImg.length() > coursesConfig.getBaseAvatarMaxSize() ||
                    !baseImg.split(",")[0].split("/")[0].equals("data:image")) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }

            draftCourseEntity.setAvatar(baseImg);
            draftCoursesRepository.save(draftCourseEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Get modules by course hash
     */
    @GetMapping(value = "/{hash}/modules")
    public String getCourseModules(Model model, @PathVariable String hash,
                                         HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/constructor";

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return "redirect:/constructor";

        model.addAttribute("modules",
                constructorService.getModules(draftCourseEntity));

        return "constructor/modules";
    }

    /**
     * Get lesson page
     */
    @GetMapping(value = "/{hash}/{moduleId}/{lessonId}")
    public String getLesson(Model model, @PathVariable String hash,
                            @RequestParam(required = false, name = "active") String activeModules,
                            @PathVariable String moduleId, @PathVariable String lessonId,
                            HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if (check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if (userEntity == null) return "redirect:/constructor";

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if (draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return "redirect:/constructor";

        constructorService.addActiveModules(model, activeModules, draftCourseEntity);
        try {
            int mid = Integer.parseInt(moduleId);
            int lid = Integer.parseInt(lessonId);

            LinkedList<Module> modules = constructorService.getModules(draftCourseEntity);
            if(mid >= modules.size()) throw new Exception();
            if(lid >= modules.get(mid).getLessons().size()) throw new Exception();

            model.addAttribute("moduleId", mid);
            model.addAttribute("lessonId", lid);
        } catch (Exception ex) {
            return "redirect:/constructor/" + hash;
        }
        model.addAttribute("course", draftCourseEntity);

        return "constructor/lesson";
    }

    /**
     * Get video of lesson
     */
    @GetMapping(value = "/{hash}/{moduleId}/{lessonId}/video")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> getVideo(@PathVariable String hash,
                                                          @PathVariable String moduleId, @PathVariable String lessonId,
                                                          HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if (check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if (userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if (draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            int mid = Integer.parseInt(moduleId);
            int lid = Integer.parseInt(lessonId);

            LinkedList<Module> modules = constructorService.getModules(draftCourseEntity);
            if(mid >= modules.size()) throw new Exception();
            if(lid >= modules.get(mid).getLessons().size()) throw new Exception();
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return coursesService.getVideoHtmlContent(
                constructorConfig.getVideosPath() + hash + "/" + moduleId + "/" + lessonId + ".mp4");
    }

    /**
     * Upload video to lesson
     */
    @PostMapping("/{hash}/{moduleId}/{lessonId}")
    public @ResponseBody ResponseEntity<Object> uploadVideo(@RequestBody byte[] bytes,
                                                            @PathVariable String hash,
                                                            @PathVariable String moduleId,
                                                            @PathVariable String lessonId,
                                                            HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if (check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if (userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if (draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            int mid = Integer.parseInt(moduleId);
            int lid = Integer.parseInt(lessonId);

            LinkedList<Module> modules = constructorService.getModules(draftCourseEntity);
            if(mid >= modules.size()) throw new Exception();
            if(lid >= modules.get(mid).getLessons().size()) throw new Exception();
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        try {
            File file = new File(constructorConfig.getVideosPath() + hash + "/" + moduleId + "/" + lessonId + ".mp4");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            loaderService.addFile(userEntity, file, bytes);
            loaderService.startFileUploading(userEntity);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Post request to delete module
     */
    @PostMapping("/{hash}/deleteModule")
    public @ResponseBody ResponseEntity<Object> deleteModule(@PathVariable String hash,
                               @RequestParam(value = "moduleId") String moduleId,
                               HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            constructorService.deleteModule(hash, Integer.parseInt(moduleId));
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Post request to add module by name
     */
    @PostMapping("/{hash}/addModule")
    public @ResponseBody ResponseEntity<Object> addModule(@PathVariable String hash,
                                                             @RequestParam(value = "module") String moduleName,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));;

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        constructorService.addModule(hash, moduleName);

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Post request to delete lesson by id
     */
    @PostMapping("/{hash}/deleteLesson")
    public @ResponseBody ResponseEntity<Object> deleteLesson(@PathVariable String hash,
                                                             @RequestParam(value = "moduleId") String moduleId,
                                                             @RequestParam(value = "lessonId") String lessonId,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            constructorService.deleteLesson(hash, Integer.parseInt(moduleId), Integer.parseInt(lessonId));
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

    /**
     * Post request to add lesson by name
     */
    @PostMapping("/{hash}/addLesson")
    public @ResponseBody ResponseEntity<Object> addLesson(@PathVariable String hash,
                                                         @RequestParam(value = "moduleId") String moduleId,
                                                         @RequestParam(value = "lesson") String lessonName,
                                                         HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        DraftCourseEntity draftCourseEntity = draftCoursesRepository.findByHash(hash).orElse(null);

        if(draftCourseEntity == null || constructorService.hasNoAccess(userEntity, draftCourseEntity))
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            constructorService.addLesson(hash, Integer.parseInt(moduleId), lessonName);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }
}
