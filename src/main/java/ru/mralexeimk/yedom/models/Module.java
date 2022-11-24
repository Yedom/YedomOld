package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
public class Module {
    private String name;
    private LinkedList<Lesson> lessons;

    public Module() {
        this.name = "";
        this.lessons = new LinkedList<>();
    }

    public Module(String name) {
        this.name = name;
        this.lessons = new LinkedList<>();
    }
}
