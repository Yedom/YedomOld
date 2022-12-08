package ru.mralexeimk.yedom.config.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Smart Search System configuration (include tags recommendation)
 * @author mralexeimk
 */
@Configuration
@ConfigurationProperties(prefix = "smart-search")
@Data
public class SmartSearchConfig {
    private int updatePeriodHours;
    private int maxRelatedTags;
    private int maxRelatedCourses;
    private int maxPopularTags;
    private int maxWordsInTag;
    private int maxLevenshteinDistance;
    private String levenshteinWeights;
    private int maxWordsInRequest;
    private int maxTagsSuggestions;

    public int[] getLevenshteinWeights() {
        String[] weights = levenshteinWeights.split(",");
        int[] result = new int[weights.length];
        for(int i = 0; i < weights.length; i++) {
            result[i] = Integer.parseInt(weights[i]);
        }
        return result;
    }
}
