package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.config.configs.OrganizationConfig;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.database.repositories.UserRepository;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.OrganizationsService;
import ru.mralexeimk.yedom.utils.services.UtilsService;
import ru.mralexeimk.yedom.utils.validators.OrganizationValidator;

import javax.servlet.http.HttpSession;

/**
 * Constructor for organization add/edit
 */
@Controller
@RequestMapping("/organization")
public class OrganizationController {
    private final UtilsService utilsService;
    private final OrganizationValidator organizationValidator;
    private final OrganizationRepository organizationRepository;
    private final OrganizationsService organizationsService;
    private final OrganizationConfig organizationConfig;
    private final UserRepository userRepository;

    public OrganizationController(UtilsService utilsService, OrganizationValidator organizationValidator, OrganizationRepository organizationRepository, OrganizationsService organizationsService, OrganizationConfig organizationConfig, UserRepository userRepository) {
        this.utilsService = utilsService;
        this.organizationValidator = organizationValidator;
        this.organizationRepository = organizationRepository;
        this.organizationsService = organizationsService;
        this.organizationConfig = organizationConfig;
        this.userRepository = userRepository;
    }

    /**
     * Redirect .../organization to .../profile/organizations
     */
    @GetMapping
    public String index() {
        return "redirect:/profile/organizations";
    }

    /**
     * Get organization creation page
     */
    @GetMapping("/add")
    public String add(@ModelAttribute Organization organization,
                      HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        return "organization/add";
    }

    /**
     * Get organization page
     */
    @GetMapping("/{organization}")
    public String getOrganization(Model model, @PathVariable String organization,
                                  HttpSession session) {
        OrganizationEntity organizationEntity = organizationRepository.findByName(organization).orElse(null);
        if(organizationEntity == null) return "redirect:/profile/organizations";

        int id = 0;
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                id = user.getId();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        model.addAttribute("organization", new Organization(organizationEntity));
        model.addAttribute("session_user_id", id);

        return "organization/organization";
    }

    /**
     * Attempt to create organization
     */
    @PostMapping("/add")
    public String addOrganization(@ModelAttribute Organization organization,
                                  BindingResult bindingResult, HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");
        UserEntity userEntity = userRepository.findById(user.getId()).orElse(null);

        if(userEntity == null) return "redirect:/profile/organizations";

        if(organizationsService.getOrganizationsCount(userEntity)
                >= organizationConfig.getMaxOrganizationsPerUser()) {
            utilsService.reject("name", "organizations.limit", bindingResult);
            return "organization/add";
        }

        organizationValidator.validate(organization.withArgs("add"), bindingResult);

        if (bindingResult.hasErrors())
            return "organization/add";

        organizationsService.createOrganization(organization, userEntity);

        return "redirect:/profile/"+user.getUsername()+"/organizations";
    }

    /**
     * Organization upload avatar
     */
    @PostMapping("/uploadAvatar")
    public @ResponseBody ResponseEntity<Object> uploadAvatar(@RequestParam(name = "orgname") String orgName,
                                                             @RequestBody String data,
                                                             HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return new ResponseEntity<>(HttpStatus.valueOf(500));

        User user = (User) session.getAttribute("user");
        OrganizationEntity organizationEntity = organizationRepository.findByName(orgName).orElse(null);

        if(organizationEntity == null || user.getId() != organizationEntity.getAdminId())
            return new ResponseEntity<>(HttpStatus.valueOf(500));

        try {
            JSONObject json = new JSONObject(data);

            String baseImg = json.getString("baseImg");

            if(baseImg.length() > organizationConfig.getBaseBannerDefault().length() ||
                    !baseImg.split(",")[0].split("/")[0].equals("data:image")) {
                return new ResponseEntity<>(HttpStatus.valueOf(500));
            }

            organizationEntity.setAvatar(baseImg);
            organizationRepository.save(organizationEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }
}
