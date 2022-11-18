package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
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
import ru.mralexeimk.yedom.utils.enums.SocketType;
import ru.mralexeimk.yedom.utils.services.DraftCoursesService;
import ru.mralexeimk.yedom.utils.services.TagsService;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/constructor")
public class ConstructorController {
    private final UtilsService utilsService;
    private final DraftCourseRepository draftCourseRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final CoursesConfig coursesConfig;
    private final OrganizationsService organizationsService;
    private final DraftCoursesService draftCoursesService;
    private final TagRepository tagRepository;

    public ConstructorController(UtilsService utilsService, DraftCourseRepository draftCourseRepository, OrganizationRepository organizationRepository, UserRepository userRepository, CoursesConfig coursesConfig, OrganizationsService organizationsService, DraftCoursesService draftCoursesService, TagRepository tagRepository) {
        this.utilsService = utilsService;
        this.draftCourseRepository = draftCourseRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.coursesConfig = coursesConfig;
        this.organizationsService = organizationsService;
        this.draftCoursesService = draftCoursesService;
        this.tagRepository = tagRepository;
    }

    /**
     * Check if UserEntity has access to DraftCourse
     */
    private boolean checkAccess(UserEntity userEntity, DraftCourseEntity draftCourseEntity) {
        if(!draftCourseEntity.isByOrganization()) {
            return userEntity.getId() == draftCourseEntity.getCreatorId();
        } else {
            return organizationsService.isMember(userEntity, draftCourseEntity.getCreatorId());
        }
    }

    /**
     * Get draft courses by section value
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
            if (sectionValue.equals("0")) {
                for (Integer id : utilsService.splitToListInt(userEntity.getDraftCoursesIds())) {
                    DraftCourseEntity draftCourseEntity = draftCourseRepository.findById(id).orElse(null);
                    if (draftCourseEntity == null) continue;
                    draftCourses.add(new DraftCourse(draftCourseEntity));
                }
            } else {
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
        if(!checkAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("draftCourse", draftCourseEntity);

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
        if(!checkAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("draftCourse", draftCourseEntity);

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
        if(!checkAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("draftCourse", draftCourseEntity);

        return "constructor/public";
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

        if(draftCourseEntity == null || !draftCoursesService.checkAccess(userEntity, draftCourseEntity))
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
}
