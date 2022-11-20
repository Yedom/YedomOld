package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Internationalization configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "language")
@Data
public class LanguageConfig {
    private String defaultLanguage;
    private List<String> languages;
}
