package ru.mralexeimk.yedom.utils.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.config.configs.OrganizationConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.database.repositories.OrganizationRepository;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.utils.services.UtilsService;

@Component
public class OrganizationValidator implements Validator {
    private final UtilsService utilsService;
    private final OrganizationConfig organizationConfig;
    private final OrganizationRepository organizationRepository;

    public OrganizationValidator(UtilsService utilsService, OrganizationConfig organizationConfig, OrganizationRepository organizationRepository) {
        this.utilsService = utilsService;
        this.organizationConfig = organizationConfig;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Organization.class.equals(aClass);
    }

    public void addValidator(Organization org, Errors errors) {
        if(org.getName() == null || org.getName().isEmpty()) {
            utilsService.reject("name", "organization.name.not.empty", errors);
        }
        else if(org.getName().length() < organizationConfig.getMinNameLength() ||
                org.getName().length() > organizationConfig.getMaxNameLength()) {
            utilsService.reject("name", "organization.name.size", errors);
        }
        else if(organizationRepository.findByName(org.getName()).isPresent()) {
            utilsService.reject("name", "organization.name.unique", errors);
        }
    }

    @Override
    public void validate(Object o, Errors errors) {
        if(o instanceof Organization org) {
            if(org.getArgs().contains("add")) {
                addValidator(org, errors);
            }
        }
    }
}
