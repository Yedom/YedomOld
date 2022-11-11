package ru.mralexeimk.yedom.utils.custom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<T, V> {
    private T first;
    private V second;
}
