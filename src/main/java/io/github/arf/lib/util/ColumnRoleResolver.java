package io.github.arf.lib.util;

import io.github.arf.lib.models.constants.ColumnRole;
import io.github.arf.lib.models.internal.Row;

import java.util.HashSet;
import java.util.Set;

public final class ColumnRoleResolver {
    private ColumnRoleResolver() {
        // utility class
    }
    /**
     * @param rowsA  all rows of table A (raw, untyped string values)
     * @param rowsB  all rows of table B
     * @param colIndexA index of the candidate column within table A's rows
     * @param colIndexB index of the candidate column within table B's rows
     * @return a two-element array: [0] = role for column A, [1] = role for column B
     */
    public static ColumnRole[] resolve(Row[] rowsA, Row[] rowsB, int colIndexA, int colIndexB) {
        boolean aIsKey = isKeyCandidate(rowsA, colIndexA);
        boolean bIsKey = isKeyCandidate(rowsB, colIndexB);

        if (aIsKey && !bIsKey) {
            return new ColumnRole[]{ColumnRole.PRIMARY_KEY_CANDIDATE, ColumnRole.FOREIGN_KEY_CANDIDATE};
        }
        if (bIsKey && !aIsKey) {
            return new ColumnRole[]{ColumnRole.FOREIGN_KEY_CANDIDATE, ColumnRole.PRIMARY_KEY_CANDIDATE};
        }
        if (aIsKey) { // both true
            return new ColumnRole[]{ColumnRole.POSSIBLE_ONE_TO_ONE_KEY, ColumnRole.POSSIBLE_ONE_TO_ONE_KEY};
        }
        return new ColumnRole[]{ColumnRole.UNKNOWN, ColumnRole.UNKNOWN};
    }
    /**
     * A column is a key candidate only if every row has a non-null,
     * non-blank value AND all values are distinct. Empty tables (zero rows)
     * are never key candidates — there is no evidence either way.
     */
    private static boolean isKeyCandidate(Row[] rows, int colIndex) {
        if (rows.length == 0) {
            return false;
        }
        Set<String> distinctValues = new HashSet<>();
        for (Row row : rows) {
            String value = row.values()[colIndex];
            if (value == null || value.isBlank()) {
                return false; // non-null gate fails immediately
            }
            distinctValues.add(value);
        }
        return distinctValues.size() == rows.length; // uniqueness gate
    }
}
