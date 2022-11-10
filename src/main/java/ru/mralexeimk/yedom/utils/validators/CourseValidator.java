package ru.mralexeimk.yedom.utils.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

@Component
public class CourseValidator implements Validator {
    private final LanguageUtil languageUtil;
    private final CoursesConfig coursesConfig;

    @Autowired
    public CourseValidator(LanguageUtil languageUtil, CoursesConfig coursesConfig) {
        this.languageUtil = languageUtil;
        this.coursesConfig = coursesConfig;
    }

    public void reject(String field, String msg, Errors errors) {
        errors.rejectValue(field, msg,
                languageUtil.getLocalizedMessage(msg));
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Course.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if(o instanceof Course course) {
            if(course.getTitle().length() < coursesConfig.getMinTitleLength() ||
                    course.getTitle().length() > coursesConfig.getMaxTitleLength()) {
                reject("title", "course.title.size", errors);
            }
            if(course.getDescription().length() > coursesConfig.getMaxDescriptionLength()) {
                reject("description", "course.description.size", errors);
            }
            if(!CommonUtils.regexMatch(coursesConfig.getRegexp(), course.getTags())
                    || course.getTags().contains("@ ") || course.getTags().contains(" @")) {
                reject("tags", "course.tags.pattern", errors);
            }
            if(CommonUtils.containsSymbols(course.getTags(), coursesConfig.getTagsDisabledSymbols())) {
                reject("tags", "course.tags.disabled_symbols", errors);
            }
            if(course.getTags().split("@").length < coursesConfig.getMinTagsCount()) {
                reject("tags", "course.tags.count", errors);
            }
            if(course.getTags().split("@").length > coursesConfig.getMaxTagsCount()) {
                reject("tags", "course.tags.max_count", errors);
            }
        }
    }
}
