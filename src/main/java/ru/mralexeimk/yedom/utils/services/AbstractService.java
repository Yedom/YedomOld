package ru.mralexeimk.yedom.utils.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import ru.mralexeimk.yedom.config.configs.AbstractServerConfig;
import ru.mralexeimk.yedom.config.configs.FriendsServerConfig;
import ru.mralexeimk.yedom.config.configs.SmartSearchServerConfig;
import ru.mralexeimk.yedom.utils.enums.SocketType;
import ru.mralexeimk.yedom.utils.multithreading.ClientSocket;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractService {
    private final AbstractServerConfig serverConfig;
    private final ConcurrentHashMap<String, ClientSocket> clientSocketByKey = new ConcurrentHashMap<>();

    protected <T extends AbstractServerConfig> AbstractService(T serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * Remove inactive sockets connections
     */
    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        if(serverConfig instanceof SmartSearchServerConfig) {
            System.out.println("SmartSearchService started!");
        }
        else if(serverConfig instanceof FriendsServerConfig) {
            System.out.println("FriendsService started!");
        }

        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000L*60*serverConfig.getMaxInactiveMinutesTime());
                } catch(Exception ignored) {}
                for(String id : clientSocketByKey.keySet()) {
                    long current_time = System.currentTimeMillis();
                    ClientSocket clientSocket = clientSocketByKey.get(id);
                    if(current_time - clientSocket.getLastActivityTime() >=
                            1000L*60*serverConfig.getMaxInactiveMinutesTime()) {
                        clientSocket.close();
                        clientSocketByKey.remove(id);
                    }
                }
            }
        }).start();
    }

    public String sendSocket(String key, SocketType socketType) {
        return sendSocket(key, socketType, "", false);
    }

    public String sendSocket(String key, SocketType socketType, String msg) {
        return sendSocket(key, socketType, msg, false);
    }

    public String sendSocket(String key, SocketType socketType, String msg, boolean resending) {
        String response = "";
        ClientSocket clientSocket = createConnection(key);
        try {
            clientSocket.getSocket().setSoTimeout(serverConfig.getTimeout());
            clientSocket.sendMessage(socketType + ":" + msg);
            response = clientSocket.receiveMessage();
            if(response.equals("exit")) {
                clientSocket.close();
                clientSocketByKey.remove(key);
                if(!resending) {
                    response = sendSocket(key, socketType, msg, true);
                }
            }
        } catch (Exception ex) {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocketByKey.remove(key);
                if(!resending)
                    return sendSocket(key, socketType, msg, true);
            }
        }
        return response;
    }

    public ClientSocket createConnection(String key) {
        ClientSocket clientSocket = null;
        try {
            if (clientSocketByKey.containsKey(key)) {
                clientSocket = clientSocketByKey.get(key);
            } else {
                clientSocket = new ClientSocket(serverConfig.getHost(), serverConfig.getPort());
                clientSocket.getSocket().setSoTimeout(serverConfig.getTimeout());
                clientSocket.sendMessage(key);
                clientSocketByKey.put(key, clientSocket);
            }
        } catch (Exception ignored) {}
        return clientSocket;
    }
}
