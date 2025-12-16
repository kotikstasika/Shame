package org.dimasik.shame.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pair<F, S> {
    @Getter
    private F first;
    @Getter
    private S second;
}