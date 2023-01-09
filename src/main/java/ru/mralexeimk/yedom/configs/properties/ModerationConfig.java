package ru.mralexeimk.yedom.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for moderation system
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "moderation")
@Data
public class ModerationConfig {
    private int maxCoursesInQueue;
    private int acceptVotesNeeded;
    private int declineVotesNeeded;
    private int coursesOnPage;
}
