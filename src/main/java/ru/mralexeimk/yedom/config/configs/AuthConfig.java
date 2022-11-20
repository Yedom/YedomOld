package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication System configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "auth")
@Data
public class AuthConfig {
    private int minPasswordLength;
    private int maxPasswordLength;
    private int minUsernameLength;
    private int maxUsernameLength;
}
