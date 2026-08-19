package io.github.arf;

import io.github.arf.lib.models.Relationship;
import io.github.arf.lib.models.Table;
import io.github.arf.lib.models.constants.ColumnRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DuplicateColumnNamesTest {

    /**
     * Parent has two columns named "Link":
     * - index 0: unique values [10, 20, 30] (PK-like)
     * - index 2: repeating values [1, 1, 2] (FK-like)
     *
     * Only index 2 overlaps with Child.ParentLink, so detection uses column 2.
     * Role resolution must also use column 2 — not resolve "Link" back to index 0.
     */
    @Test
    void duplicateColumnNames_rolesUseDetectedColumnIndex() {
        Table<String> parent = new Table<>("Parent",
                List.of("Link", "Name", "Link"),
                List.of(
                        List.of("10", "a", "1"),
                        List.of("20", "b", "1"),
                        List.of("30", "c", "2")
                ));

        Table<String> child = new Table<>("Child",
                List.of("ChildID", "ParentLink"),
                List.of(
                        List.of("c1", "1"),
                        List.of("c2", "1"),
                        List.of("c3", "2"),
                        List.of("c4", "3")
                ));

        AutomaticRelationshipFinder<String> finder =
                new AutomaticRelationshipFinder.AutomaticRelationshipFinderBuilder<>(
                        List.of(parent, child))
                        .setColumnNameConfidence(0.4)
                        .setDataConfidence(0.5)
                        .builder();

        List<Relationship> relationships = finder.findRelationShip();

        Relationship match = relationships.stream()
                .filter(r -> r.fromTable().equals("Parent")
                        && r.fromColumn().columnName().equals("Link")
                        && r.toTable().equals("Child")
                        && r.toColumn().columnName().equals("ParentLink"))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Expected relationship between Parent.Link and Child.ParentLink"));

        // Column 2 has repeating values, so it must not be labelled as a primary key.
        // Before the fix, getColumnIndex("Link") always returned 0, where it's values
        // [10, 20, 30] incorrectly produced PRIMARY_KEY_CANDIDATE.
        assertNotEquals(
                ColumnRole.PRIMARY_KEY_CANDIDATE,
                match.fromColumn().role(),
                "Role should reflect column index 2 (repeating values), not index 0");
        assertEquals(ColumnRole.UNKNOWN, match.fromColumn().role());
    }
}
