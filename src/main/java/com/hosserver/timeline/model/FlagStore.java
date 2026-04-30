package com.hosserver.timeline.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-unsafe key-value store for server-state flags.
 * All keys are normalised to lower-case. Values are raw strings.
 */
public final class FlagStore {

    private final Map<String, String> flags = new HashMap<>();

    public void set(String key, String value) {
        flags.put(key.toLowerCase(), value);
    }

    public String get(String key) {
        return flags.get(key.toLowerCase());
    }

    public boolean has(String key) {
        return flags.containsKey(key.toLowerCase());
    }

    /** Returns true only when the key exists AND its value equals the given string (case-sensitive value). */
    public boolean matches(String key, String value) {
        return value.equals(flags.get(key.toLowerCase()));
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(flags);
    }

    public void clear() {
        flags.clear();
    }

    /** Bulk-load from persisted data. Normalises keys. */
    public void putAll(Map<String, String> source) {
        source.forEach((k, v) -> flags.put(k.toLowerCase(), v));
    }
}
