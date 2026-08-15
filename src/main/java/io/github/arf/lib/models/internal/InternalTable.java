package io.github.arf.lib.models.internal;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record InternalTable(String tableName, ColumnInfo[] columns, Row[] rows, String[] columnNames,Map<String, Integer> columnIndexLookup) {

    public InternalTable(String tableName, ColumnInfo[] columns, Row[] rows) {
        this(tableName, columns, rows, extractColumnNames(columns),createColumnIndexLookup(columns));
    }

    private static String[] extractColumnNames(
            ColumnInfo[] columns) {

        String[] names =
                new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            names[i] = columns[i].name();
        }

        return names;
    }

    private static Map<String, Integer> createColumnIndexLookup(ColumnInfo[] columns){
        return IntStream.range(0, columns.length)
                .boxed()
                .collect(Collectors.toMap(
                        index -> columns[index].name(),
                        index -> index,
                        (existing,replacement)->existing
                ));
    }

    public int getColumnIndex(String columnName){
        return columnIndexLookup.get(columnName);
    }

    public String getColumnName(ColumnSet columnSet) {
        if (columnSet.columnIndexes().length != 1) {
            throw new IllegalArgumentException(
                    "Expected a single-column ColumnSet"
            );
        }
        return columns[columnSet.columnIndexes()[0]].name();
    }
}
