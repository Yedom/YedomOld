package ru.mralexeimk.yedom.utils.language;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Class for getting localized messages from code (messages.properties)
 */
public class LanguageUtil {
    @Autowired
    private MessageSource messageSource;

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }

    public void setLocale(String locale) {
        LocaleContextHolder.setLocale(new Locale(locale));
    }

    public String getLocalizedMessage(String messageKey) {
        return messageSource.getMessage(messageKey, null, getLocale());
    }

    public String getLocalizedMessage(String messageKey, String... args) {
        return messageSource.getMessage(messageKey, args, getLocale());
    }
}
