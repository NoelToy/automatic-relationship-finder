package io.github.arf.lib.models;

import io.github.arf.lib.models.constants.ColumnRole;

import java.util.Objects;

/**
 * Binds a column's name to its inferred {@link ColumnRole} within a specific
 * {@link Relationship}, so the two facts can never be transposed or
 * separated by callers.
 */
public record RelationshipColumn(String columnName, ColumnRole role) {
    public RelationshipColumn {
        Objects.requireNonNull(columnName, "columnName must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
