package ru.mralexeimk.yedom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mralexeimk.yedom.utils.EmailService;

@SpringBootApplication
public class YedomApplication {
    public static void main(String[] args) {
        SpringApplication.run(YedomApplication.class, args);
        EmailService.start();
    }
}
