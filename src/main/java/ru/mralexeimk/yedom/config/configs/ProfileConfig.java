package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "profile")
@Data
public class ProfileConfig {
    private String baseAvatarDefault;
    private int baseAvatarMaxSize;
}
