package io.github.arf.lib.models;

import java.util.List;
import java.util.Objects;

public record Table<T>(String name, List<String> columns, List<List<T>> data) {
    public Table {
        Objects.requireNonNull(name, "Table name must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("Table name must not be blank");
        Objects.requireNonNull(columns, "columns must not be null");
        Objects.requireNonNull(data, "data must not be null");
        if (columns.isEmpty()) throw new IllegalArgumentException("Table '" + name + "' has no columns");
        for (int i = 0; i < data.size(); i++) {
            List<T> row = data.get(i);
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException(
                        "Table '" + name + "' row " + i + " has " + row.size() +
                                " values but " + columns.size() + " columns were declared");
            }
        }
    }
}
