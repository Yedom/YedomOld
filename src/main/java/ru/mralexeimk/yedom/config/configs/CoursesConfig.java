package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "courses")
@Data
public class CoursesConfig {
    private int minTitleLength;
    private int maxTitleLength;
    private int maxDescriptionLength;
    private String baseAvatarDefault;
    private int minTagsCount;
    private int maxTagsCount;
    private String tagsDisabledSymbols;
    private String regexp;
}
