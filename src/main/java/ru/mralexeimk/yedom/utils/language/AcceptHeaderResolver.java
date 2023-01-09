package ru.mralexeimk.yedom.utils.language;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import ru.mralexeimk.yedom.configs.properties.LanguageConfig;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.services.UtilsService;

import java.util.List;
import java.util.Locale;

/**
 * Component for resolving locale from request header or user setting
 */
@Component
public class AcceptHeaderResolver extends AcceptHeaderLocaleResolver {
    private final UtilsService utilsService;
    private final List<Locale> LOCALES;

    public AcceptHeaderResolver(UtilsService utilsService, LanguageConfig languageConfig) {
        this.utilsService = utilsService;
        LOCALES = languageConfig.getLanguages().stream().map(Locale::new).toList();
    }

    @Override
    public @NonNull Locale resolveLocale(@NonNull HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String check = utilsService.preventUnauthorizedAccess(session);
                if (check == null) {
                    User user = (User) session.getAttribute("user");
                    String lang = user.getSettings().getLang();
                    if (!lang.equals("auto")) return new Locale(lang);
                }
            }
            if (!StringUtils.hasLength(request.getHeader("Accept-Language"))) {
                return Locale.getDefault();
            }
            List<Locale.LanguageRange> list = Locale.LanguageRange.parse(request.getHeader("Accept-Language"));
            return Locale.lookup(list, LOCALES);
        } catch (Exception ex) {
            return Locale.getDefault();
        }
    }
}