package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "organizations")
@Data
public class OrganizationConfig {
    private int minNameLength;
    private int maxNameLength;
    private String baseBannerDefault;
    private int maxOrganizationsPerUser;
}
