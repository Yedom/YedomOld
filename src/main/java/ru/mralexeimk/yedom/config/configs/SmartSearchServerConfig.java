package ru.mralexeimk.yedom.config.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Smart Search remote (by sockets) Server Configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "ss-server")
public class SmartSearchServerConfig extends AbstractServerConfig {
}
