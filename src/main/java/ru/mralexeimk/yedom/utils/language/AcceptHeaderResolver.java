package ru.mralexeimk.yedom.utils.language;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.services.UtilsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class AcceptHeaderResolver extends AcceptHeaderLocaleResolver {
    private final UtilsService utilsService;
    private final List<Locale> LOCALES = Arrays.asList(
            new Locale("ru"),
            new Locale("en")
    );

    public AcceptHeaderResolver(UtilsService utilsService) {
        this.utilsService = utilsService;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            String check = utilsService.preventUnauthorizedAccess(session);
            if(check == null) {
                User user = (User) session.getAttribute("user");
                String lang = user.getSettings().getLang();
                if(!lang.equals("auto")) return new Locale(lang);
            }
        }
        if (StringUtils.isEmpty(request.getHeader("Accept-Language"))) {
            return Locale.getDefault();
        }
        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(request.getHeader("Accept-Language"));
        return Locale.lookup(list, LOCALES);
    }
}