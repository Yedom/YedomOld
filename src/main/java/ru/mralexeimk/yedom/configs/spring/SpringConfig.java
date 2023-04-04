package ru.mralexeimk.yedom.configs.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import ru.mralexeimk.yedom.configs.properties.LanguageConfig;
import ru.mralexeimk.yedom.services.UtilsService;
import ru.mralexeimk.yedom.utils.language.AcceptHeaderResolver;
import ru.mralexeimk.yedom.utils.language.LanguageUtil;

import java.util.Properties;

/**
 * Spring configuration
 */
@PropertySource("classpath:/application.properties")
@PropertySource("classpath:/application-${spring.profiles.active}.properties")
@Configuration
@ComponentScan("ru.mralexeimk.yedom")
@EnableWebMvc
public class SpringConfig implements WebMvcConfigurer {
    private final ApplicationContext applicationContext;

    private final ThymeleafProperties thymeleafProperties;
    @Value("${spring.thymeleaf.templates_root:}")
    private String templatesRoot;

    @Autowired
    public SpringConfig(ApplicationContext applicationContext, ThymeleafProperties thymeleafProperties) {
        this.applicationContext = applicationContext;
        this.thymeleafProperties = thymeleafProperties;
    }

    @Bean
    public ITemplateResolver defaultTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(".html");
        resolver.setPrefix(templatesRoot);
        resolver.setTemplateMode(thymeleafProperties.getMode());
        resolver.setCacheable(thymeleafProperties.isCache());
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(defaultTemplateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        registry.viewResolver(resolver);
    }

    /**
     * Set resource handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/**")
                .addResourceLocations("/resources/")
                .addResourceLocations("/WEB-INF/resources/");
    }

    /**
     * Database connection by properties ('spring.datasource.*')
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Email sender settings
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("argentochest@gmail.com");
        mailSender.setPassword("bpnugzstmmakbojs");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.from.email", "argentochest@gmail.com");

        return mailSender;
    }

    /**
     * Resolve locale by 'Accept-Language' header or by user's settings
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderResolver(
                applicationContext.getBean(UtilsService.class),
                applicationContext.getBean(LanguageConfig.class));
    }

    /**
     * LanguageUtil for getting translated messages from code
     */
    @Bean
    public LanguageUtil languageUtil() {
        return new LanguageUtil();
    }

    /**
     * Message source for configuring localed messages in files
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:language/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public MappingJackson2HttpMessageConverter messageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        return converter;
    }

    /**
     * Fix: No converter for [class org.springframework.boot.actuate.endpoint.OperationResponseBodyMap]
     */
    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter(MappingJackson2HttpMessageConverter messageConverter) {
        RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        adapter.getMessageConverters().add(messageConverter);
        return adapter;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }
}
