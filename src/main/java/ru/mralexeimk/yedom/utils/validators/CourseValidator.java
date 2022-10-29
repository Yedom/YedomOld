package ru.mralexeimk.yedom.utils.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.models.Course;
import ru.mralexeimk.yedom.utils.CommonUtils;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

@Component
public class CourseValidator implements Validator {
    private final LanguageUtil languageUtil;

    @Autowired
    public CourseValidator(LanguageUtil languageUtil) {
        this.languageUtil = languageUtil;
    }

    public void reject(String field, String msg, Errors errors) {
        errors.rejectValue(field, "",
                languageUtil.getLocalizedMessage(msg));
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Course.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if(o instanceof Course course) {
            if(!CommonUtils.regexMatch(course.getTags(), YedomConfig.REGEX_TAGS)
                    || course.getTags().contains("@ ") || course.getTags().contains(" @")) {
                reject("tags", "course.tags.pattern", errors);
            }
            if(CommonUtils.containsSymbols(course.getTags(), YedomConfig.TAGS_DISABLED_SYMBOLS)) {
                reject("tags", "course.tags.disabled_symbols", errors);
            }
            if(course.getTags().split("@").length < YedomConfig.MIN_TAGS_COUNT) {
                reject("tags", "course.tags.count", errors);
            }
            if(course.getTags().split("@").length > YedomConfig.MAX_TAGS_COUNT) {
                reject("tags", "course.tags.max_count", errors);
            }
        }
    }
}
