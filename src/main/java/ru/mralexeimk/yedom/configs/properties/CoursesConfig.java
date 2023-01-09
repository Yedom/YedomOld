package ru.mralexeimk.yedom.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Courses creation/updating configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "courses")
@Data
public class CoursesConfig {
    private String videosPath;
    private int minTitleLength;
    private int maxTitleLength;
    private int maxDescriptionLength;
    private String baseAvatarDefault;
    private int baseAvatarMaxSize;
    private int minTagsCount;
    private int maxTagsCount;
    private String tagsDisabledSymbols;
    private String regexp;
}
