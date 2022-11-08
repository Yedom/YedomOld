package ru.mralexeimk.yedom.models;

import lombok.Data;

@Data
public class Pair<T, V> {
    private T first;
    private V second;

    public Pair(T first, V second) {
        this.first = first;
        this.second = second;
    }
}
