package ru.mralexeimk.yedom.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "friends-system")
@Data
public class FriendsConfig {
    private int saveToDBPeriodHours;
    private int maxConnectedUsers;
}
