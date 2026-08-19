package io.github.arf.lib.models.internal;

public record InternalTable(String tableName, ColumnInfo[] columns, Row[] rows, String[] columnNames) {

    public InternalTable(String tableName, ColumnInfo[] columns, Row[] rows) {
        this(tableName, columns, rows, extractColumnNames(columns));
    }

    private static String[] extractColumnNames(ColumnInfo[] columns) {
        String[] names = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            names[i] = columns[i].name();
        }
        return names;
    }

    public String getColumnName(int columnIndex) {
        return columns[columnIndex].name();
    }

    public String getColumnName(ColumnSet columnSet) {
        return getColumnName(columnSet.singleIndex());
    }
}
