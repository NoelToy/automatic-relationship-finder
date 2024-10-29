package io.github.arf.lib.models;

public record Relationship(String fromTable, String toTable, String fromColumnName, String toColumnName) {
}
