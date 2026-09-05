package io.github.arf.lib.models.internal;

public record ColumnSet(int[] columnIndexes) {
    public static ColumnSet single(int index) {
        return new ColumnSet(
                new int[]{index}
        );
    }

    public int singleIndex() {
        if (columnIndexes.length != 1) {
            throw new IllegalArgumentException("Expected a single-column ColumnSet");
        }
        return columnIndexes[0];
    }
}
