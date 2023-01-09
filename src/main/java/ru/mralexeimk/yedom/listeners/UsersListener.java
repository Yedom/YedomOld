package ru.mralexeimk.yedom.listeners;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer listener for 'users' topic
 */
@Component
public class UsersListener {
    @KafkaListener(topics = "users", groupId = "group-id")
    public void consume(String message) {
        System.out.println("Consumed message: " + message);
    }
}
