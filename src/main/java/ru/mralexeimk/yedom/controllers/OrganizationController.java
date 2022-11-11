package ru.mralexeimk.yedom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.mralexeimk.yedom.models.Organization;

@Controller
@RequestMapping("/organization")
public class OrganizationController {

    @GetMapping
    public String index(Model model) {


        return "organization/index";
    }

    @GetMapping("/{organization}")
    public String getOrganization(Model model, @PathVariable Organization organization) {


        return "organizations/organization";
    }
}
