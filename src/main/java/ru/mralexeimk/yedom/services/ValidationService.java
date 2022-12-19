package ru.mralexeimk.yedom.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {
    private final LanguageUtil languageUtil;

    @Autowired
    public ValidationService(LanguageUtil languageUtil) {
        this.languageUtil = languageUtil;
    }

    /**
     * Models validation reject
     */
    public void reject(String field, String msg, Errors errors) {
        if(!errors.hasFieldErrors(field)) {
            errors.rejectValue(field, msg,
                    languageUtil.getLocalizedMessage(msg));
        }
    }

    /**
     * Check if string match regex
     */
    public boolean regexMatch(String regex, String str) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }
}
