package ru.mralexeimk.yedom.utils.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.SmartSearchServerConfig;
import ru.mralexeimk.yedom.utils.enums.SocketType;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.multithreading.ClientSocket;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Service for working with smart search server
 */
@Service
public class TagsService extends AbstractService {
    @Autowired
    public TagsService(SmartSearchServerConfig smartSearchServerConfig) {
        super(smartSearchServerConfig);
    }
}
