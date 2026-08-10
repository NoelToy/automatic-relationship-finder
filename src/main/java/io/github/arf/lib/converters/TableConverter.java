package io.github.arf.lib.converters;

import io.github.arf.lib.models.Table;
import io.github.arf.lib.models.internal.ColumnInfo;
import io.github.arf.lib.models.internal.InternalTable;
import io.github.arf.lib.models.internal.Row;
import io.github.arf.lib.util.ListToArray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableConverter {
    public static <T>List<InternalTable> convertAll(List<Table<T>> tables){
        return tables.stream().map(TableConverter::convert)
                .collect(Collectors.toList());
    }
    public static <T> InternalTable convert(Table<T> table){

        ColumnInfo[] columns = IntStream.range(0, table.columns().size())
                .mapToObj(i -> new ColumnInfo(i, table.columns().get(i)))
                .toArray(ColumnInfo[]::new);

        Row[] rows = table.data().stream().map(ts -> {
            String[] values = ListToArray.convertToArray(ts);
            return new Row(values);
        }).toArray(Row[]::new);

        return new InternalTable(table.name(), columns, rows);
    }
}
