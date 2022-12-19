package ru.mralexeimk.yedom.utils.validators;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.config.configs.OrganizationConfig;
import ru.mralexeimk.yedom.database.repositories.OrganizationsRepository;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.utils.enums.OrganizationValidationType;
import ru.mralexeimk.yedom.services.OrganizationsService;
import ru.mralexeimk.yedom.services.ValidationService;

/**
 * Organization model validator
 */
@Component
public class OrganizationValidator implements Validator {
    private final ValidationService validationService;
    private final OrganizationConfig organizationConfig;
    private final OrganizationsService organizationsService;
    private final OrganizationsRepository organizationsRepository;

    public OrganizationValidator(ValidationService validationService, OrganizationConfig organizationConfig, OrganizationsService organizationsService, OrganizationsRepository organizationsRepository) {
        this.validationService = validationService;
        this.organizationConfig = organizationConfig;
        this.organizationsService = organizationsService;
        this.organizationsRepository = organizationsRepository;
    }

    @Override
    public boolean supports(@NonNull Class<?> aClass) {
        return Organization.class.equals(aClass);
    }

    public void addValidator(Organization org, Errors errors) {
        if(organizationsService.getOrganizationsCount(org.getCreator())
                >= organizationConfig.getMaxOrganizationsPerUser()) {
            validationService.reject("name", "organizations.limit", errors);
        }
        else if(org.getName() == null || org.getName().isEmpty()) {
            validationService.reject("name", "organization.name.not.empty", errors);
        }
        else if(org.getName().length() < organizationConfig.getMinNameLength() ||
                org.getName().length() > organizationConfig.getMaxNameLength()) {
            validationService.reject("name", "organization.name.size", errors);
        }
        else if(organizationsRepository.findByName(org.getName()).isPresent()) {
            validationService.reject("name", "organization.name.unique", errors);
        }
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        if(o instanceof Organization org) {
            if(org.getValidationType() == OrganizationValidationType.CREATE) {
                addValidator(org, errors);
            }
        }
    }
}
