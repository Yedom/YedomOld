package ru.mralexeimk.yedom.utils.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.SmartSearchServerConfig;
import ru.mralexeimk.yedom.utils.enums.SocketType;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.multithreading.ClientSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class TagsService {
    private final SmartSearchServerConfig smartSearchServerConfig;
    private final Map<String, ClientSocket> clientSocketByEmail = new HashMap<>();
    private final Map<String, ClientSocket> clientSocketByIp = new HashMap<>();

    @Autowired
    public TagsService(SmartSearchServerConfig smartSearchServerConfig) {
        this.smartSearchServerConfig = smartSearchServerConfig;
    }

    public String sendSocket(User user, SocketType socketType) {
        return sendSocket(user, socketType, "", false);
    }

    public String sendSocket(User user, SocketType socketType, String msg) {
        return sendSocket(user, socketType, msg, false);
    }

    /**
     * Send socket to SmartSearchServer and get response
     */
    public String sendSocket(User user, SocketType socketType, String msg, boolean resending) {
        String response = "";
        ClientSocket clientSocket = createConnection(user);
        try {
            clientSocket.getSocket().setSoTimeout(smartSearchServerConfig.getTimeout());
            clientSocket.sendMessage(socketType + ":" + msg);
            response = clientSocket.receiveMessage();
        } catch (Exception ex) {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocketByEmail.remove(user.getEmail());
                if(!resending)
                    return sendSocket(user, socketType, msg, true);
            }
            ex.printStackTrace();
        }
        return response;
    }

    public String sendSocket(String ip, SocketType socketType) {
        return sendSocket(ip, socketType, "", false);
    }

    public String sendSocket(String ip, SocketType socketType, String msg) {
        return sendSocket(ip, socketType, msg, false);
    }

    /**
     * Send socket to SmartSearchServer and get response
     */
    public String sendSocket(String ip, SocketType socketType, String msg, boolean resending) {
        String response = "";
        ClientSocket clientSocket = createConnection(ip);
        try {
            clientSocket.getSocket().setSoTimeout(smartSearchServerConfig.getTimeout());
            clientSocket.sendMessage(socketType + ":" + msg);
            response = clientSocket.receiveMessage();
        } catch (Exception ex) {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocketByIp.remove(ip);
                if(!resending)
                    return sendSocket(ip, socketType, msg, true);
            }
            ex.printStackTrace();
        }
        return response;
    }

    /**
     * Create connection between Java client and Python server with multithreading sockets
     */
    public ClientSocket createConnection(User user) {
        ClientSocket clientSocket = null;
        try {
            if (clientSocketByEmail.containsKey(user.getEmail())) {
                clientSocket = clientSocketByEmail.get(user.getEmail());
            } else {
                clientSocket = new ClientSocket(smartSearchServerConfig.getHost(), smartSearchServerConfig.getPort());
                clientSocket.getSocket().setSoTimeout(smartSearchServerConfig.getTimeout());
                clientSocket.sendMessage(user.getEmail());
                clientSocketByEmail.put(user.getEmail(), clientSocket);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return clientSocket;
    }

    public ClientSocket createConnection(String ip) {
        ClientSocket clientSocket = null;
        try {
            if (clientSocketByIp.containsKey(ip)) {
                clientSocket = clientSocketByIp.get(ip);
            } else {
                clientSocket = new ClientSocket(smartSearchServerConfig.getHost(), smartSearchServerConfig.getPort());
                clientSocket.getSocket().setSoTimeout(smartSearchServerConfig.getTimeout());
                clientSocket.sendMessage(ip);
                clientSocketByIp.put(ip, clientSocket);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return clientSocket;
    }
}
