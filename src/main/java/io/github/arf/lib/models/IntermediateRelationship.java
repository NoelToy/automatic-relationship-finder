package io.github.arf.lib.models;

public record IntermediateRelationship(int fromTableIndex, int toTableIndex, int fromColumnIndex, int toColumnIndex, boolean isColumnNameMatch,double dataSimilarity) {
}
