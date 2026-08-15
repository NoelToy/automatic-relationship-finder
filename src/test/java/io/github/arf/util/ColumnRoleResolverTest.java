package io.github.arf.util;

import io.github.arf.lib.models.constants.ColumnRole;
import io.github.arf.lib.models.internal.Row;
import io.github.arf.lib.util.ColumnRoleResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ColumnRoleResolverTest {
    private Row[] rowsOf(String... values) {
        Row[] rows = new Row[values.length];
        for (int i = 0; i < values.length; i++) {
            rows[i] = new Row(new String[]{values[i]});
        }
        return rows;
    }
    @Test
    void aIsPrimaryKey_bIsForeignKey() {
        Row[] a = rowsOf("1", "2", "3");
        Row[] b = rowsOf("1", "1", "2");
        assertArrayEquals(
                new ColumnRole[]{ColumnRole.PRIMARY_KEY_CANDIDATE, ColumnRole.FOREIGN_KEY_CANDIDATE},
                ColumnRoleResolver.resolve(a, b, 0, 0));
    }

    @Test
    void bIsPrimaryKey_aIsForeignKey() {
        Row[] a = rowsOf("1", "1", "2");
        Row[] b = rowsOf("1", "2", "3");
        assertArrayEquals(
                new ColumnRole[]{ColumnRole.FOREIGN_KEY_CANDIDATE, ColumnRole.PRIMARY_KEY_CANDIDATE},
                ColumnRoleResolver.resolve(a, b, 0, 0));
    }

    @Test
    void bothUnique_areAmbiguous() {
        Row[] a = rowsOf("1", "2", "3");
        Row[] b = rowsOf("x", "y", "z");
        assertArrayEquals(
                new ColumnRole[]{ColumnRole.POSSIBLE_ONE_TO_ONE_KEY, ColumnRole.POSSIBLE_ONE_TO_ONE_KEY},
                ColumnRoleResolver.resolve(a, b, 0, 0));
    }

    @Test
    void neitherUnique_areUnknown() {
        Row[] a = rowsOf("1", "1", "2");
        Row[] b = rowsOf("x", "x", "y");
        assertArrayEquals(
                new ColumnRole[]{ColumnRole.UNKNOWN, ColumnRole.UNKNOWN},
                ColumnRoleResolver.resolve(a, b, 0, 0));
    }

    @Test
    void nullOrBlankValue_disqualifiesKey() {
        Row[] a = rowsOf("1", null, "3");
        Row[] b = rowsOf("1", "2", "3");
        assertArrayEquals(
                new ColumnRole[]{ColumnRole.FOREIGN_KEY_CANDIDATE, ColumnRole.PRIMARY_KEY_CANDIDATE},
                ColumnRoleResolver.resolve(a, b, 0, 0));
    }

    @Test
    void emptyRows_areUnknown() {
        Row[] a = new Row[0];
        Row[] b = new Row[0];
        assertArrayEquals(
                new ColumnRole[]{ColumnRole.UNKNOWN, ColumnRole.UNKNOWN},
                ColumnRoleResolver.resolve(a, b, 0, 0));
    }
}
