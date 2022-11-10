package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ss-server")
@Data
public class SmartSearchServerConfig {
    private String host;
    private int port;
    private int timeout;
}
