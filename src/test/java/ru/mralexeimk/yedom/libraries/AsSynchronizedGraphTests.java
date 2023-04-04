package ru.mralexeimk.yedom.libraries;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AsSynchronizedGraphTests {
    private final AsSynchronizedGraph<String, DefaultEdge> graph;

    @Autowired
    @Lazy
    public AsSynchronizedGraphTests() {
        graph = new AsSynchronizedGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));
    }

    @Test
    void baseChecks() {
        graph.addVertex("java");
        graph.addVertex("python");
        graph.addVertex("javascript");
        graph.addVertex("html 5");
        graph.addVertex("программирование");
        graph.addEdge("java", "python");
        graph.addEdge("java", "javascript");
        graph.addEdge("java", "html 5");
        graph.addEdge("программирование", "python");

        assertTrue(graph.containsVertex("java"));
        assertTrue(graph.containsVertex("python"));
        assertTrue(graph.containsVertex("javascript"));
        assertTrue(graph.containsVertex("html 5"));
        assertTrue(graph.containsVertex("программирование"));
        assertTrue(graph.containsEdge("java", "python"));
        assertTrue(graph.containsEdge("java", "javascript"));
        assertTrue(graph.containsEdge("java", "html 5"));
        assertTrue(graph.containsEdge("программирование", "python"));

        assertTrue(Graphs.neighborListOf(graph, "java").contains("python"));
        assertTrue(Graphs.neighborListOf(graph, "java").contains("javascript"));
        assertTrue(Graphs.neighborListOf(graph, "java").contains("html 5"));
        assertTrue(Graphs.neighborListOf(graph, "программирование").contains("python"));
    }
}
