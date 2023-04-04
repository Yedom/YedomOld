package ru.mralexeimk.yedom.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import ru.mralexeimk.yedom.configs.properties.SmartSearchConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TagsServiceTests {
    private final TagsService tagsService;
    private final SmartSearchConfig smartSearchConfig;

    @Autowired
    @Lazy
    public TagsServiceTests(TagsService tagsService, SmartSearchConfig smartSearchConfig) {
        this.tagsService = tagsService;
        this.smartSearchConfig = smartSearchConfig;
    }

    @Test
    void levenshteinDistance() {
        int[] weights = smartSearchConfig.getLevenshteinWeights();
        assertEquals(0, tagsService.levenshteinDistance("test", "test"));
        assertEquals(0, tagsService.levenshteinDistance("", ""));
        assertEquals(weights[0], tagsService.levenshteinDistance("test", "test1"));
        assertEquals(weights[1], tagsService.levenshteinDistance("test2", "test"));
        assertEquals(Math.min(weights[0] + weights[1], weights[2]),
                tagsService.levenshteinDistance("tesv", "test"));
    }

    @Test
    void getEnumerated() {
        assertEquals(List.of(List.of("1", "2"), List.of("2", "1")),
                tagsService.getEnumerated(List.of("1", "2"), 2, true));
        assertEquals(List.of(List.of("1"), List.of("2")),
                tagsService.getEnumerated(List.of("1", "2"), 1, true));
        assertEquals(List.of(
                        List.of("1", "2", "3"),
                        List.of("1", "3", "2"),
                        List.of("2", "1", "3"),
                        List.of("2", "3", "1"),
                        List.of("3", "1", "2"),
                        List.of("3", "2", "1")
                    ),
                tagsService.getEnumerated(List.of("1", "2", "3"), 3, true));
        assertEquals(List.of(
                        List.of("1", "2"),
                        List.of("1", "3"),
                        List.of("2", "1"),
                        List.of("2", "3"),
                        List.of("3", "1"),
                        List.of("3", "2")
                ),
                tagsService.getEnumerated(List.of("1", "2", "3"), 2, true));
    }

    @Test
    void parseInputToTags() {
        assertEquals(Set.of("программирование", "java"),
                tagsService.parseInputToTags("программирование на java"));
        assertEquals(Set.of("программирование", "python"),
                tagsService.parseInputToTags("прогроммированние на pithon"));
        assertEquals(Set.of("python"),
                tagsService.parseInputToTags("python"));
        assertEquals(Set.of("java"),
                tagsService.parseInputToTags("java"));
        assertEquals(Set.of("java", "java spring"),
                tagsService.parseInputToTags("основы java spring"));
    }

    @Test
    void getRelatedTags() {
        assertEquals(Set.of("python", "pandas", "java", "java spring", "hibernate"),
                tagsService.getRelatedTags("программирование"));
        assertEquals(Set.of("программирование", "pandas"),
                tagsService.getRelatedTags("python"));
        assertEquals(Set.of("программирование", "pandas", "java", "java spring", "hibernate"),
                tagsService.getRelatedTags("python", 2));
    }

    @Test
    void searchCoursesByTag() {
        assertEquals(List.of(1, 2),
                tagsService.searchCoursesByTag("программирование").stream()
                        .map(CourseEntity::getId).sorted().toList());
        assertEquals(List.of(1),
                tagsService.searchCoursesByTag("python").stream()
                        .map(CourseEntity::getId).sorted().toList());
        assertEquals(List.of(2),
                tagsService.searchCoursesByTag("java spring").stream()
                        .map(CourseEntity::getId).sorted().toList());
    }

    @Test
    void searchCoursesByInput() {
        assertEquals(List.of(1, 2),
                tagsService.searchCoursesByInput("программироване").stream()
                        .map(CourseEntity::getId).sorted().toList());
        assertEquals(List.of(1, 2),
                tagsService.searchCoursesByInput("оснновы программирования на jaava").stream()
                        .map(CourseEntity::getId).sorted().toList());
        assertEquals(List.of(1),
                tagsService.searchCoursesByInput("pithon для новичков").stream()
                        .map(CourseEntity::getId).sorted().toList());
        assertEquals(List.of(),
                tagsService.searchCoursesByInput("йцуроорццукер").stream()
                        .map(CourseEntity::getId).sorted().toList());
    }
}
