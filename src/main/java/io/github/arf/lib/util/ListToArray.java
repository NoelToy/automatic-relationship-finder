package io.github.arf.lib.util;

import java.util.List;

public class ListToArray {
    public static <T> String[] convertToArray(List<T> input){
        if (input==null||input.isEmpty()) {
            return new String[0];
        }
        return input.stream()
                .map(item->{
                    if (item==null) {
                        return null;
                    }
                    return item.toString();
                }).toArray(String[]::new);
    }
    public static <T> String[][] convertTo2DArray(List<List<T>> input){
        if (input==null||input.isEmpty()) {
            return new String[0][0];
        }
        return input.stream()
                .map(inner->inner.stream()
                        .map(item->{
                            if (item==null) {
                                return null;
                            }
                            return item.toString();
                        }).toArray(String[]::new)
                ).toArray(String[][]::new);
    }
}
