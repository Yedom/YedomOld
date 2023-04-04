package ru.mralexeimk.yedom.libraries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class KafkaTests {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Lazy
    public KafkaTests(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Test
    public void testKafka() {
        kafkaTemplate.send("users", "test");
    }
}
