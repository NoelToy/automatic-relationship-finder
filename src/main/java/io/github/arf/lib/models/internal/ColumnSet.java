package io.github.arf.lib.models.internal;

public record ColumnSet(int[] columnIndexes) {
    public static ColumnSet single(int index) {
        return new ColumnSet(
                new int[]{index}
        );
    }
}
