package ru.mralexeimk.yedom.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * User's profiles configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "profile")
@Data
public class ProfileConfig {
    private String baseAvatarDefault;
    private int baseAvatarMaxSize;
    private int maxLinks;
}
