package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "draft-courses")
@Data
public class DraftCoursesConfig {
    private int maxPerUser;
    private int maxPerOrganization;
    private int daysAlive;
}
