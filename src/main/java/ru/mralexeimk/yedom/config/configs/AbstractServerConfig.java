package ru.mralexeimk.yedom.config.configs;

import lombok.Data;

/**
 * Abstract class for remote (by sockets) server configuration
 * @author mralexeimk
 */
@Data
public class AbstractServerConfig {
    private String host;
    private int port;
    private int timeout;
    private int maxInactiveMinutesTime;
}
