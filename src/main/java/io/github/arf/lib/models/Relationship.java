package io.github.arf.lib.models;

public record Relationship(String fromTable,
                           String toTable,
                           RelationshipColumn fromColumn,
                           RelationshipColumn toColumn,
                           double dataSimilarity) {
    /**
     * @deprecated since 1.2.0, will be removed in 1.3.0.
     * Use {@link #fromColumn()}{@code .columnName()} instead.
     */
    @Deprecated(since = "1.2", forRemoval = true)
    public String fromColumnName() {
        return fromColumn.columnName();
    }

    /**
     * @deprecated since 1.2.0, will be removed in 1.3.0.
     * Use {@link #toColumn()}{@code .columnName()} instead.
     */
    @Deprecated(since = "1.2", forRemoval = true)
    public String toColumnName() {
        return toColumn.columnName();
    }
}
