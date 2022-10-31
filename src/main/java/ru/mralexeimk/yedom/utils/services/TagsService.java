package ru.mralexeimk.yedom.utils.services;

import ch.qos.logback.core.net.server.Client;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.enums.SocketType;
import ru.mralexeimk.yedom.models.Code;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.utils.multithreading.ClientSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class TagsService {
    private final Map<String, ClientSocket> clientSocketByEmail = new HashMap<>();

    public String sendSocket(User user, SocketType socketType) {
        return sendSocket(user, socketType, "", false);
    }

    public String sendSocket(User user, SocketType socketType, String msg) {
        return sendSocket(user, socketType, msg, false);
    }

    public String sendSocket(User user, SocketType socketType, String msg, boolean resending) {
        String response = "";
        try {
            ClientSocket clientSocket = createConnection(user);
            if(!clientSocket.isActive()) {
                clientSocket.activate();
                clientSocket.getSocket().setSoTimeout(YedomConfig.REC_TIMEOUT);

                clientSocket.sendMessage(socketType.toString() + ":" + msg);
                response = clientSocket.receiveMessage();
                clientSocket.deactivate();
            }
        } catch (Exception ex) {
            if (clientSocketByEmail.containsKey(user.getEmail())) {
                ClientSocket clientSocket = clientSocketByEmail.get(user.getEmail());
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
            if (clientSocketByEmail.containsKey(user.getEmail())
            && clientSocketByEmail.get(user.getEmail()).isAlive()) {
                clientSocket = clientSocketByEmail.get(user.getEmail());
            } else {
                clientSocket = new ClientSocket();
                clientSocket.getSocket().setSoTimeout(YedomConfig.REC_TIMEOUT);
                clientSocket.sendMessage(user.getEmail());
                clientSocketByEmail.put(user.getEmail(), clientSocket);
            }
        } catch (Exception ignored) {}
        return clientSocket;
    }

    public List<Integer> responseIdsToList(String response) {
        return Stream.of(response.split(","))
                .map(Integer::parseInt).toList();
    }
}
