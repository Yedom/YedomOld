package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TagsGraphNode {
    private int index;
    private List<Integer> courses;

    @Override
    public String toString() {
        return "TagsGraphNode{" +
                "index=" + index +
                ", courses=" + courses +
                '}';
    }
}
