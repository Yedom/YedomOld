package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Big file uploading configuration
 */
@Configuration
@ConfigurationProperties(prefix = "bigfile")
@Data
public class BigFileConfig {
    private int sizeOfPart;
    private long uploadingPeriod;
}
