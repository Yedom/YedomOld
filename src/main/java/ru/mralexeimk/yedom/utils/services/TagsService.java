package ru.mralexeimk.yedom.utils.services;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.YedomConfig;
import ru.mralexeimk.yedom.enums.SocketType;
import ru.mralexeimk.yedom.models.Code;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Service
public class TagsService {
    public String sendSocket(SocketType socketType) {
        return sendSocket(socketType, "");
    }

    public String sendSocket(SocketType socketType, String msg) {
        String response = "";
        try (Socket socket = new Socket(YedomConfig.HOST, YedomConfig.REC_PORT);
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             DataInputStream din = new DataInputStream(socket.getInputStream())) {
            socket.setSoTimeout(YedomConfig.REC_TIMEOUT);

            dout.writeUTF(socketType.toString() + ":" + msg);
            dout.flush();

            response = din.readUTF();
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
