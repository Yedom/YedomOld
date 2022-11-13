package ru.mralexeimk.yedom.config.configs;

import lombok.Data;

@Data
public class AbstractServerConfig {
    private String host;
    private int port;
    private int timeout;
    private int maxInactiveMinutesTime;
}
