package com.hosserver.timeline.model;

public enum EventType {
    BROADCAST,
    CONSOLE_COMMAND,
    SET_FLAG;

    /**
     * Case-insensitive parse. Returns null for unknown values
     * so callers can log a warning rather than throw.
     */
    public static EventType fromString(String raw) {
        if (raw == null) return null;
        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
