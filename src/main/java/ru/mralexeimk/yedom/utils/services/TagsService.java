package ru.mralexeimk.yedom.utils.services;

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
        return sendSocket(user, socketType, "");
    }

    public String sendSocket(User user, SocketType socketType, String msg) {
        String response = "";
        try {
            ClientSocket clientSocket;
            if (clientSocketByEmail.containsKey(user.getEmail()) &&
                    clientSocketByEmail.get(user.getEmail()).isAlive()) {
                clientSocket = clientSocketByEmail.get(user.getEmail());
            } else {
                clientSocket = new ClientSocket();
                clientSocket.sendMessage(user.getEmail());
                clientSocketByEmail.put(user.getEmail(), clientSocket);
            }
            clientSocket.sendMessage(socketType.toString() + ":" + msg);
            response = clientSocket.receiveMessage();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    public List<Integer> responseIdsToList(String response) {
        return Stream.of(response.split(","))
                .map(Integer::parseInt).toList();
    }
}
