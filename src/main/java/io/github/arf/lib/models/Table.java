package io.github.arf.lib.models;

import java.util.List;

public record Table<T>(String name, List<String> columns, List<List<T>> data) {
}
