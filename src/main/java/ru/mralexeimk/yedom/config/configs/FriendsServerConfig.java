package ru.mralexeimk.yedom.config.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Friends System remote (by sockets) Server Configuration
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "fs-server")
public class FriendsServerConfig extends AbstractServerConfig {
}
