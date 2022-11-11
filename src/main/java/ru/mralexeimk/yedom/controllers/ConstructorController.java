package ru.mralexeimk.yedom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mralexeimk.yedom.database.entities.DraftCourseEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.DraftCourseRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/constructor")
public class ConstructorController {
    private final UtilsService utilsService;
    private final DraftCourseRepository draftCourseRepository;
    private final UserRepository userRepository;
    private final OrganizationsService organizationsService;

    public ConstructorController(UtilsService utilsService, DraftCourseRepository draftCourseRepository, UserRepository userRepository, OrganizationsService organizationsService) {
        this.utilsService = utilsService;
        this.draftCourseRepository = draftCourseRepository;
        this.userRepository = userRepository;
        this.organizationsService = organizationsService;
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
     * Get user's draft courses
     */
    @GetMapping()
    public String index(Model model, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        List<DraftCourse> draftCourses = new ArrayList<>();

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/errors/notfound";

        for(Integer id : utilsService.splitToListInt(userEntity.getDraftCoursesIds())) {
            DraftCourseEntity draftCourseEntity = draftCourseRepository.findById(id).orElse(null);
            if(draftCourseEntity == null) continue;
            draftCourses.add(new DraftCourse(draftCourseEntity));
        }

        model.addAttribute("draft_courses", draftCourses);

        return "constructor/index";
    }

    /**
     * Open draft course constructor page
     */
    @GetMapping("/{hash}")
    public String draftCourse(Model model, @PathVariable String hash, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/constructor";
        if(!checkAccess(userEntity, draftCourseEntity)) return "redirect:/constructor";

        model.addAttribute("draftCourse", draftCourseEntity);

        return "constructor/draftcourse";
    }
}
