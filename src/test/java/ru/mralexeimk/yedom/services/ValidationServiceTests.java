package ru.mralexeimk.yedom.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import ru.mralexeimk.yedom.config.configs.CoursesConfig;
import ru.mralexeimk.yedom.config.configs.EmailConfig;
import ru.mralexeimk.yedom.services.ValidationService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ValidationServiceTests {
    private final ValidationService validationService;
    private final EmailConfig emailConfig;
    private final CoursesConfig coursesConfig;

    @Autowired
    @Lazy
    public ValidationServiceTests(ValidationService validationService, EmailConfig emailConfig, CoursesConfig coursesConfig) {
        this.validationService = validationService;
        this.emailConfig = emailConfig;
        this.coursesConfig = coursesConfig;
    }

    @Test
    void regexMatchEmail() {
        String regex = emailConfig.getRegexp();
        assertTrue(validationService.regexMatch(regex, "mralexeimk@yandex.ru"));
        assertTrue(validationService.regexMatch(regex, "argentochest@gmail.com"));
        assertFalse(validationService.regexMatch(regex, ""));
        assertFalse(validationService.regexMatch(regex, "randomString"));
        assertFalse(validationService.regexMatch(regex, "awfawf@"));
        assertFalse(validationService.regexMatch(regex, "awfawf@aw"));
        assertFalse(validationService.regexMatch(regex, "awfawf@awaw"));
        assertFalse(validationService.regexMatch(regex, "awfawf@awaw."));
    }

    @Test
    void regexMatchCourseTags() {
        String regex = coursesConfig.getRegexp();
        assertTrue(validationService.regexMatch(regex, "java"));
        assertTrue(validationService.regexMatch(regex, "java for beginners"));
        assertTrue(validationService.regexMatch(regex, "программирование"));
        assertTrue(validationService.regexMatch(regex, "java@python"));
        assertTrue(validationService.regexMatch(regex, "java@python@javascript"));
        assertTrue(validationService.regexMatch(regex, "java@python@javascript@html 5"));
        assertTrue(validationService.regexMatch(regex, "программирование@python@для всяких новичков"));
    }
}
