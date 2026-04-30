package com.hosserver.timeline.model;

/**
 * A single prerequisite that must pass before a timeline event fires.
 *
 * YAML examples:
 *
 *   # flag must equal "true"
 *   - flag: ww2_active
 *     value: "true"
 *
 *   # flag must NOT equal "occupied"
 *   - flag: poland_status
 *     value: "occupied"
 *     operator: NOT_EQUALS
 *
 *   # flag key must exist (any value)
 *   - flag: germany_mobilized
 *     operator: EXISTS
 *
 *   # flag key must not exist at all
 *   - flag: peace_treaty
 *     operator: NOT_EXISTS
 */
public final class EventCondition {

    private final String            flag;
    private final String            value;    // used by EQUALS / NOT_EQUALS; ignored otherwise
    private final ConditionOperator operator;

    /** Full constructor. */
    public EventCondition(String flag, String value, ConditionOperator operator) {
        this.flag     = flag;
        this.value    = value != null ? value : "";
        this.operator = operator != null ? operator : ConditionOperator.EQUALS;
    }

    /** Convenience constructor for the common EQUALS case (backward-compatible). */
    public EventCondition(String flag, String value) {
        this(flag, value, ConditionOperator.EQUALS);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String            getFlag()     { return flag; }
    public String            getValue()    { return value; }
    public ConditionOperator getOperator() { return operator; }

    // ── Evaluation ────────────────────────────────────────────────────────────

    /**
     * Returns true if this condition passes against the given FlagStore.
     * All evaluation logic lives here so callers never switch on the operator.
     */
    public boolean evaluate(FlagStore flagStore) {
        return switch (operator) {
            case EQUALS     ->  flagStore.matches(flag, value);
            case NOT_EQUALS -> !flagStore.matches(flag, value);
            case EXISTS     ->  flagStore.has(flag);
            case NOT_EXISTS -> !flagStore.has(flag);
        };
    }

    @Override
    public String toString() {
        return "Condition{flag='" + flag + "', op=" + operator
                + (operator == ConditionOperator.EQUALS || operator == ConditionOperator.NOT_EQUALS
                   ? ", value='" + value + "'" : "")
                + "}";
    }
}
