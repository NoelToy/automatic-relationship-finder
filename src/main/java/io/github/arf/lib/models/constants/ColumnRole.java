package io.github.arf.lib.models.constants;
/**
 * Describes the inferred structural role of a column within a single
 * detected {@link io.github.arf.lib.models.Relationship}.
 * <p>
 * This is a pairwise, relative classification — it is only meaningful in the
 * context of the specific column it was paired against, not an absolute
 * statement that a column is "the" primary key of its table.
 */
public enum ColumnRole {
    /** This side is 100% distinct and non-null; the other side is not. */
    PRIMARY_KEY_CANDIDATE,

    /** The other side is 100% distinct and non-null; this side is not. */
    FOREIGN_KEY_CANDIDATE,

    /** Both sides are 100% distinct and non-null — likely a 1:1 relationship
     *  or two independent surrogate keys; no clear PK/FK direction. */
    POSSIBLE_ONE_TO_ONE_KEY,

    /** Neither side is 100% distinct and non-null; no key-like structure
     *  detected on either side of this relationship. */
    UNKNOWN
}
