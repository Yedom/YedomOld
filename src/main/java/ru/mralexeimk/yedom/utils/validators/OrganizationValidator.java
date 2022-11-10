package ru.mralexeimk.yedom.utils.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.config.configs.OrganizationConfig;
import ru.mralexeimk.yedom.models.Organization;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

@Component
public class OrganizationValidator implements Validator {
    private final LanguageUtil languageUtil;
    private final OrganizationConfig organizationConfig;

    public OrganizationValidator(LanguageUtil languageUtil, OrganizationConfig organizationConfig) {
        this.languageUtil = languageUtil;
        this.organizationConfig = organizationConfig;
    }

    public void reject(String field, String msg, Errors errors) {
        errors.rejectValue(field, msg,
                languageUtil.getLocalizedMessage(msg));
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Organization.class.equals(aClass);
    }

    @Override
    public void validate(Object o, org.springframework.validation.Errors errors) {
        if(o instanceof Organization org) {
            if(org.getName() == null || org.getName().isEmpty()) {
                reject("name", "organization.name.not.empty", errors);
            }
            else if(org.getName().length() < organizationConfig.getMinNameLength() ||
                    org.getName().length() > organizationConfig.getMaxNameLength()) {
                reject("name", "organization.name.size", errors);
            }
        }
    }
}
