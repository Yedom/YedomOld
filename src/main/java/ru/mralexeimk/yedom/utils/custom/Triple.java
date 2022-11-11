package ru.mralexeimk.yedom.utils.custom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Triple<T, V, K> {
    private T first;
    private V second;
    private K third;
}
