package ru.mralexeimk.yedom.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.models.Code;
import ru.mralexeimk.yedom.models.User;

import java.util.HashMap;

@Service
public class EmailService {
    private static final HashMap<User, Code> codeByUser = new HashMap<>();
    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendMessage(String to, String subject, String text) {
        new Thread(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("argentochest@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        }).start();
    }

    public static void start() {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000*60*10);
                } catch(Exception ignored) {}
                for(User user : codeByUser.keySet()) {
                    long current_time = System.currentTimeMillis();
                    if(current_time - codeByUser.get(user).getStartTime() >= 1000*60*5) {
                        removeCode(user);
                    }
                }
            }
        }).start();
    }

    public static void removeCode(User user) {
        codeByUser.remove(user);
    }

    public static void saveCode(User user, Code code) {
        codeByUser.put(user, code);
    }

    public static HashMap<User, Code> getCodeByUser() {
        return codeByUser;
    }

    public static Code getRandomCode() {
        return new Code(String.valueOf(CommonUtils.getRandomNumber(100000, 999999)));
    }

    public static boolean isCorrectCode(String code) {
        try {
            int num = Integer.parseInt(code);
            if(num >= 100000 && num <= 999999) return true;
        } catch(Exception ignored) {}
        return false;
    }
}