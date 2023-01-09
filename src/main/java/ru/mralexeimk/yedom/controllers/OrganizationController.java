package ru.mralexeimk.yedom.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.mralexeimk.yedom.configs.properties.OrganizationConfig;
import ru.mralexeimk.yedom.database.entities.OrganizationEntity;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationsRepository;
import ru.mralexeimk.yedom.database.repositories.UsersRepository;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.enums.OrganizationValidationType;
import ru.mralexeimk.yedom.services.OrganizationsService;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.utils.validators.OrganizationValidator;

import jakarta.servlet.http.HttpSession;

/**
 * Constructor for organization add/edit
 */
@Controller
@RequestMapping("/organization")
public class OrganizationController {
    private final UtilsService utilsService;
    private final OrganizationValidator organizationValidator;
    private final OrganizationsRepository organizationsRepository;
    private final OrganizationsService organizationsService;
    private final OrganizationConfig organizationConfig;
    private final UsersRepository usersRepository;

    public OrganizationController(UtilsService utilsService, OrganizationValidator organizationValidator, OrganizationsRepository organizationsRepository, OrganizationsService organizationsService, OrganizationConfig organizationConfig, UsersRepository usersRepository) {
        this.utilsService = utilsService;
        this.organizationValidator = organizationValidator;
        this.organizationsRepository = organizationsRepository;
        this.organizationsService = organizationsService;
        this.organizationConfig = organizationConfig;
        this.usersRepository = usersRepository;
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
        OrganizationEntity organizationEntity = organizationsRepository.findByName(organization).orElse(null);
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
        UserEntity userEntity = usersRepository.findById(user.getId()).orElse(null);
        if(userEntity == null) return "redirect:/profile/organizations";

        organization.setCreator(userEntity);
        organizationValidator.validate(
                organization.checkFor(OrganizationValidationType.CREATE), bindingResult);

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
        OrganizationEntity organizationEntity = organizationsRepository.findByName(orgName).orElse(null);

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
            organizationsRepository.save(organizationEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.valueOf(500));
        }

        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }
}
