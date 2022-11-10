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
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/constructor")
public class ConstructorController {
    private final DraftCourseRepository draftCourseRepository;
    private final UserRepository userRepository;
    private final OrganizationsService organizationsService;

    public ConstructorController(DraftCourseRepository draftCourseRepository, UserRepository userRepository, OrganizationsService organizationsService) {
        this.draftCourseRepository = draftCourseRepository;
        this.userRepository = userRepository;
        this.organizationsService = organizationsService;
    }

    private boolean checkAccess(UserEntity userEntity, DraftCourseEntity draftCourseEntity) {
        if(!draftCourseEntity.isByOrganization()) {
            return userEntity.getId() == draftCourseEntity.getCreatorId();
        } else {
            return organizationsService.isMember(userEntity, draftCourseEntity.getCreatorId());
        }
    }

    @GetMapping()
    public String index() {
        return "lk/draftCourses";
    }

    @GetMapping("/{hash}")
    public String draftCourse(Model model, @PathVariable String hash, HttpSession session) {
        String check = CommonUtils.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);
        DraftCourseEntity draftCourseEntity = draftCourseRepository.findByHash(hash).orElse(null);

        if(userEntity == null || draftCourseEntity == null) return "redirect:/lk/draftCourses";
        if(!checkAccess(userEntity, draftCourseEntity)) return "redirect:/lk/draftCourses";

        model.addAttribute("draftCourse", draftCourseEntity);

        return "lk/constructor/draftCourse";
    }
}
