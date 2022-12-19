package ru.mralexeimk.yedom.utils.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.EmailConfig;
import ru.mralexeimk.yedom.models.Code;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {
    private final UtilsService utilsService;
    private final JavaMailSender emailSender;
    private final EmailConfig emailConfig;

    private final ConcurrentHashMap<String, Code> codeByUsername = new ConcurrentHashMap<>();

    @Autowired
    public EmailService(UtilsService utilsService, JavaMailSender emailSender, EmailConfig emailConfig) {
        this.utilsService = utilsService;
        this.emailSender = emailSender;
        this.emailConfig = emailConfig;
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

    /**
     * Remove not used codes
     */
    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        System.out.println("Email service started!");
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000L * emailConfig.getConfirmCodeTimeout());
                } catch(Exception ignored) {}
                for(String userName : codeByUsername.keySet()) {
                    long current_time = System.currentTimeMillis();
                    if(current_time - codeByUsername.get(userName).getStartTime() >=
                            1000L*emailConfig.getConfirmCodeTimeout()) {
                        removeCode(userName);
                    }
                }
            }
        }).start();
    }

    public void removeCode(String userName) {
        codeByUsername.remove(userName);
    }

    public void saveCode(String userName, Code code) {
        codeByUsername.put(userName, code);
    }

    public ConcurrentHashMap<String, Code> getCodeByUser() {
        return codeByUsername;
    }

    public Code getRandomCode() {
        return new Code(String.valueOf(utilsService.getRandomNumber(100000, 999999)));
    }

    public boolean isCorrectCode(String code) {
        try {
            int num = Integer.parseInt(code);
            if(num >= Math.pow(10, emailConfig.getConfirmCodeLength() - 1) &&
                    num < Math.pow(10, emailConfig.getConfirmCodeLength())) return true;
        } catch(Exception ignored) {}
        return false;
    }
}