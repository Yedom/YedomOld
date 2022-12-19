package ru.mralexeimk.yedom.services;

import lombok.Getter;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.SmartSearchConfig;
import ru.mralexeimk.yedom.database.entities.CourseEntity;
import ru.mralexeimk.yedom.database.repositories.CoursesRepository;
import ru.mralexeimk.yedom.models.TagsGraphNode;
import ru.mralexeimk.yedom.utils.custom.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for working with smart search system
 * (<a href="https://github.com/Yedom/Articles/blob/main/SmartSearch/Documentation.pdf">How it works</a>)
 * @author mralexeimk
 */
@Service
public class TagsService {
    private final LogsService logsService;
    private final SmartSearchConfig smartSearchConfig;
    private final CoursesRepository coursesRepository;

    @Getter
    private AsSynchronizedGraph<String, DefaultEdge> graph;
    @Getter
    private ConcurrentHashMap<String, TagsGraphNode> graphInfo;
    @Getter
    private List<String> sortedTags;
    @Getter
    private List<String> popularTags;

    public TagsService(LogsService logsService, SmartSearchConfig smartSearchConfig, CoursesRepository coursesRepository) {
        this.logsService = logsService;
        this.smartSearchConfig = smartSearchConfig;
        this.coursesRepository = coursesRepository;

        graph = new AsSynchronizedGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));
        graphInfo = new ConcurrentHashMap<>();
        sortedTags = new ArrayList<>();
        popularTags = new ArrayList<>();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        logsService.info("Service started: " + this.getClass().getSimpleName());
        update();
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(smartSearchConfig.getUpdatePeriodHours() * 60 * 60 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                update();
            }
        }).start();
    }

    /**
     * Update graph from 'courses' table
     */
    public void update() {
        logsService.info("Updating tags graph...");

        // update 'graphInfo'
        graphInfo = new ConcurrentHashMap<>();

        coursesRepository.findAll().forEach(course -> {
            String[] tags = course.getTags().split("@");
            for (String tag : tags) {
                graphInfo.putIfAbsent(tag, new TagsGraphNode(
                        graphInfo.size(), new ArrayList<>()));

                if(graphInfo.get(tag).getCourses().size()
                        < smartSearchConfig.getMaxRelatedCourses()) {
                    graphInfo.get(tag).getCourses().add(course.getId());
                }
                // replace course with max number of tags course
                else {
                    int maxTagsCount = 0;
                    int maxTagsCountIndex = 0;
                    for (int i = 0; i < graphInfo.get(tag).getCourses().size(); i++) {
                        CourseEntity courseEntity = coursesRepository.findById(
                                graphInfo.get(tag).getCourses().get(i)).orElse(null);
                        if(courseEntity == null) continue;
                        int tagsCount = courseEntity.getTags().split("@").length;
                        if(tagsCount > maxTagsCount) {
                            maxTagsCount = tagsCount;
                            maxTagsCountIndex = i;
                        }
                    }
                    if(maxTagsCount < tags.length) {
                        graphInfo.get(tag).getCourses().set(maxTagsCountIndex, course.getId());
                    }
                }
            }
        });

        // update 'graph'
        graph = new AsSynchronizedGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));

        coursesRepository.findAll().forEach(course -> {
            String[] tags = course.getTags().split("@");

            for(String tag : tags) {
                graph.addVertex(tag);
            }

            for (int i = 0; i < tags.length; i++) {
                List<String> neighbors = Graphs.neighborListOf(graph, tags[i]);
                for (int j = 0; j < tags.length; j++) {
                    if (i != j) {
                        if(neighbors.size() < smartSearchConfig.getMaxRelatedTags()) {
                            graph.addEdge(tags[i], tags[j]);
                        }
                        // replace neighbor with min courses count
                        else {
                            String minCoursesCountNeighbor = neighbors.get(0);
                            for (String neighbor : neighbors) {
                                if (graphInfo.get(neighbor).getCourses().size() <
                                        graphInfo.get(minCoursesCountNeighbor).getCourses().size()) {
                                    minCoursesCountNeighbor = neighbor;
                                }
                            }
                            if (graphInfo.get(tags[j]).getCourses().size() >
                                    graphInfo.get(minCoursesCountNeighbor).getCourses().size()) {
                                graph.removeEdge(tags[i], minCoursesCountNeighbor);
                                graph.addEdge(tags[i], tags[j]);
                            }
                        }
                    }
                }
            }
        });

        // update 'sortedTags'
        sortedTags = new ArrayList<>(graphInfo.keySet());
        sortedTags.sort(Comparator.naturalOrder());

        // update 'popularTags'
        popularTags = new ArrayList<>();
        List<Pair<Integer, String>> tagsWithCoursesCount = new ArrayList<>();

        graphInfo.forEach((tag, graphNode) ->
                tagsWithCoursesCount.add(new Pair<>(graphNode.getCourses().size(), tag)));

        tagsWithCoursesCount.sort((o1, o2) -> o2.getFirst() - o1.getFirst());

        for (int i = 0; i < Math.min(smartSearchConfig.getMaxPopularTags(), tagsWithCoursesCount.size()); i++) {
            popularTags.add(tagsWithCoursesCount.get(i).getSecond());
        }

        logsService.info("Popular tags (" + smartSearchConfig.getMaxPopularTags() + "):" +
                popularTags.toString());
    }

    /**
     * Levenshtein distance between two strings
     */
    public int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        int[] weights = smartSearchConfig.getLevenshteinWeights();

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j * weights[1];
                }
                else if (j == 0) {
                    dp[i][j] = i * weights[0];
                }
                else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] +
                                    (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : weights[2]),
                            Math.min(dp[i - 1][j] + weights[0],
                                    dp[i][j - 1] + weights[1]));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Get all permutations
     */
    public List<List<String>> getEnumerated(List<String> a, int k, boolean sorted) {
        List<List<String>> allPermutations = new ArrayList<>();
        enumerate(new LinkedList<>(a), a.size(), k, allPermutations);
        if(sorted) {
            allPermutations.sort((o1, o2) -> {
                for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
                    int compare = o1.get(i).compareTo(o2.get(i));
                    if(compare != 0) return compare;
                }
                return 0;
            });
        }
        return allPermutations;
    }

    public List<List<String>> getEnumerated(List<String> a, int k) {
        return getEnumerated(a, k, false);
    }

    private void enumerate(LinkedList<String> a,
                          int n,
                          int k,
                          List<List<String>> allPermutations) {
        if (k == 0) {
            List<String> singlePermutation = new ArrayList<>();
            for (int i = n; i < a.size(); i++) {
                singlePermutation.add(a.get(i));
            }
            allPermutations.add(singlePermutation);
            return;
        }
        for (int i = 0; i < n; i++) {
            Collections.swap(a, i, n - 1);
            enumerate(a, n - 1, k - 1, allPermutations);
            Collections.swap(a, i, n - 1);
        }
    }

    private List<String> parseInputToTags(String input) {
        List<String> tags = new ArrayList<>();

        input = input.replaceAll("\\s+", " ");
        List<String> words = Arrays.asList(input.split(" "));
        for(int k = 1; k <= smartSearchConfig.getMaxWordsInTag(); ++k) {
            getEnumerated(words, k).forEach(combination -> {
                String hypothesisTag = String.join(" ", combination);

                // binary search in 'sortedTags' closest to 'hypothesisTag' by Levenshtein distance
                int l = 0, r = sortedTags.size() - 1;
                while (l < r) {
                    int m = (l + r) / 2;
                    if (levenshteinDistance(hypothesisTag, sortedTags.get(m)) <
                            levenshteinDistance(hypothesisTag, sortedTags.get(m + 1))) {
                        r = m;
                    }
                    else {
                        l = m + 1;
                    }
                }
                if (levenshteinDistance(hypothesisTag, sortedTags.get(l)) <=
                        smartSearchConfig.getMaxLevenshteinDistance()) {
                    tags.add(sortedTags.get(l));
                }
            });
        }

        return tags;
    }

    /**
     * Get neighbors of 'tag'
     */
    public Set<String> getRelatedTags(String tag) {
        return getRelatedTags(tag, 1);
    }

    /**
     * Get neighbors of 'tag' (and neighbors of neighbors) with depth 'depth'
     */
    public Set<String> getRelatedTags(String tag, int depth) {
        Set<String> relatedTags = new HashSet<>();
        Set<String> lastRelatedTags = new HashSet<>();
        lastRelatedTags.add(tag);
        for (int i = 0; i < depth; i++) {
            Set<String> newRelatedTags = new HashSet<>();
            for (String lastRelatedTag : lastRelatedTags) {
                newRelatedTags.addAll(Graphs.neighborListOf(graph, lastRelatedTag));
                newRelatedTags.remove(tag);
            }
            relatedTags.addAll(newRelatedTags);
            lastRelatedTags = newRelatedTags;
        }
        return relatedTags;
    }

    /**
     * Parse 'inputStr' to list of tags and get related tags to them
     */
    public Set<String> searchRelatedTags(String inputStr) {
        Set<String> relatedTags = new HashSet<>();
        List<String> tags = parseInputToTags(inputStr);
        for (String tag : tags) {
            relatedTags.addAll(getRelatedTags(tag));
        }
        return relatedTags;
    }

    /**
     * Search courses connected to 'tag'
     */
    public Set<CourseEntity> searchCoursesByTag(String tag) {
        Set<CourseEntity> courses = new HashSet<>();
        if(graphInfo.containsKey(tag)) {
            graphInfo.get(tag).getCourses().forEach(courseId ->
                    coursesRepository.findById(courseId).ifPresent(courses::add));
        }
        return courses;
    }

    /**
     * Parse 'inputStr' to list of tags and get related courses to them
     */
    public Set<CourseEntity> searchCoursesByInput(String inputStr) {
        Set<CourseEntity> courses = new HashSet<>();
        List<String> tags = parseInputToTags(inputStr);
        for (String tag : tags) {
            courses.addAll(searchCoursesByTag(tag));
        }
        return courses;
    }
}
