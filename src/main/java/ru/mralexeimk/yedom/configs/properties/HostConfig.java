package ru.mralexeimk.yedom.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Host Configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "host")
@Data
public class HostConfig {
    private String link;
}
