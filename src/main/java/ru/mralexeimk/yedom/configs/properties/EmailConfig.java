package ru.mralexeimk.yedom.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Email Service Configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "email")
@Data
public class EmailConfig {
    private int confirmCodeTimeout;
    private int confirmCodeLength;
    private String regexp;
}