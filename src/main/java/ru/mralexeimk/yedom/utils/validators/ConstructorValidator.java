package ru.mralexeimk.yedom.utils.validators;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.configs.properties.CoursesConfig;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.models.DraftCourse;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.services.ValidationService;

/**
 * Draft course model validator
 */
@Component
public class ConstructorValidator implements Validator {
    private final UtilsService utilsService;
    private final ValidationService validationService;
    private final CoursesConfig coursesConfig;

    @Autowired
    public ConstructorValidator(UtilsService utilsService, ValidationService validationService, CoursesConfig coursesConfig) {
        this.utilsService = utilsService;
        this.validationService = validationService;
        this.coursesConfig = coursesConfig;
    }

    @Override
    public boolean supports(@NonNull Class<?> aClass) {
        return Course.class.equals(aClass);
    }

    @Override
    public void validate(@NonNull Object o, @NonNull Errors errors) {
        if(o instanceof DraftCourse draftCourse) {
            if(draftCourse.getTitle().length() < coursesConfig.getMinTitleLength() ||
                    draftCourse.getTitle().length() > coursesConfig.getMaxTitleLength()) {
                validationService.reject("title", "course.title.size", errors);
            }
            if(draftCourse.getDescription().length() > coursesConfig.getMaxDescriptionLength()) {
                validationService.reject("description", "course.description.size", errors);
            }
            if(!validationService.regexMatch(coursesConfig.getRegexp(), draftCourse.getTags())
                    || draftCourse.getTags().contains("@ ") || draftCourse.getTags().contains(" @")) {
                validationService.reject("tags", "course.tags.pattern", errors);
            }
            if(utilsService.containsAnySymbols(draftCourse.getTags(), coursesConfig.getTagsDisabledSymbols())) {
                validationService.reject("tags", "course.tags.disabled_symbols", errors);
            }
            if(draftCourse.getTags().split("@").length < coursesConfig.getMinTagsCount()) {
                validationService.reject("tags", "course.tags.count", errors);
            }
            if(draftCourse.getTags().split("@").length > coursesConfig.getMaxTagsCount()) {
                validationService.reject("tags", "course.tags.max_count", errors);
            }
        }
    }
}
