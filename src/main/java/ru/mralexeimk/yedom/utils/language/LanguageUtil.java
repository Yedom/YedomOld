package ru.mralexeimk.yedom.utils.language;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageUtil {
    @Autowired
    private MessageSource messageSource;

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public String getLocalizedMessage(String messageKey) {
        return messageSource.getMessage(messageKey, null, getLocale());
    }

    public String getLocalizedMessage(String messageKey, String... args) {
        return messageSource.getMessage(messageKey, args, getLocale());
    }
}
