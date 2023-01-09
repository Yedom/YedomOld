package ru.mralexeimk.yedom.configs;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import lombok.NonNull;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import ru.mralexeimk.yedom.configs.spring.SpringConfig;

/**
 * Default Spring MVC Dispatcher Servlet Initializer
 */
public class MySpringMvcDispatcherSerlvetIntitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{SpringConfig.class};
    }

    @Override
    protected String @NonNull [] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    public void onStartup(@NonNull ServletContext aServletContext) throws ServletException {
        super.onStartup(aServletContext);
        registerHiddenFieldFilter(aServletContext);
    }

    private void registerHiddenFieldFilter(ServletContext aContext) {
        aContext.addFilter("hiddenHttpMethodFilter",
                new HiddenHttpMethodFilter()).addMappingForUrlPatterns(null ,true, "/*");
    }
}
