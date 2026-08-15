package io.github.arf.lib.models;

import io.github.arf.lib.models.internal.ColumnSet;

public record IntermediateRelationship(int fromTableIndex, int toTableIndex, ColumnSet fromColumns, ColumnSet toColumns, boolean isColumnNameMatch, double dataSimilarity) {
}
