package ru.mralexeimk.yedom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.mralexeimk.yedom.utils.services.EmailService;
import ru.mralexeimk.yedom.utils.services.RolesService;

@SpringBootApplication
public class YedomApplication {
    public static void main(String[] args) {
        SpringApplication.run(YedomApplication.class, args);
    }
}
