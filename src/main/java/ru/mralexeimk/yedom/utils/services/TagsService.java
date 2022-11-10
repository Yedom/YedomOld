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

    public List<Integer> responseIdsToList(String response) {
        return Stream.of(response.split(","))
                .map(Integer::parseInt).toList();
    }
}
