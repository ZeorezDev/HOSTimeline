package com.hosserver.timeline.model;

/**
 * Comparison operator for timeline event conditions.
 *
 * EQUALS / NOT_EQUALS  — require the 'value' field in the condition.
 * EXISTS / NOT_EXISTS  — 'value' is ignored; only the key's presence matters.
 */
public enum ConditionOperator {

    /** Flag must equal the specified value. Default when operator is omitted. */
    EQUALS,

    /** Flag must NOT equal the specified value. */
    NOT_EQUALS,

    /** Flag key must exist (any value accepted). */
    EXISTS,

    /** Flag key must NOT exist. */
    NOT_EXISTS;

    /**
     * Case-insensitive parse. Returns EQUALS for null or unknown strings
     * so existing configs without an 'operator' field keep working.
     */
    public static ConditionOperator fromString(String raw) {
        if (raw == null) return EQUALS;
        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EQUALS;
        }
    }
}
