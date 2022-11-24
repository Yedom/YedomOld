package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Draft courses configuration
 */
@Configuration
@ConfigurationProperties(prefix = "draft-courses")
@Data
public class DraftCoursesConfig {
    private int maxPerUser;
    private int maxPerOrganization;
    private int daysAlive;
    private int maxModuleAndLessonNameLength;
    private int maxModules;
    private int maxLessons;
    private String disabledSymbols;
    private int saveToDBPeriodMinutes;
}
