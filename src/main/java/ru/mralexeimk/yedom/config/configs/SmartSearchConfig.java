package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Smart Search System configuration (include tags recommendation)
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "ss")
@Data
public class SmartSearchConfig {
    private int maxWordsInRequest;
    private int maxTagsSuggestions;
}
