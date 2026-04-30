package com.hosserver.timeline.storage;

import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.FlagStore;

import java.util.Set;

/** Abstraction over persistence. Swap YamlStorageImpl for SQL later without touching callers. */
public interface StorageManager {

    /** Load all persisted state into memory. Called once on startup. */
    void load();

    /** Flush all in-memory state to disk. */
    void save();

    // ── Date ────────────────────────────────────────────────────────────────

    TimelineDate getCurrentDate();

    void setCurrentDate(TimelineDate date);

    // ── Fired events ─────────────────────────────────────────────────────────

    /** Unmodifiable view of fired event IDs. */
    Set<String> getFiredEvents();

    void addFiredEvent(String eventId);

    // ── Flags ────────────────────────────────────────────────────────────────

    FlagStore getFlagStore();

    // ── Auto-advance pause state ─────────────────────────────────────────────

    boolean isPaused();

    void setPaused(boolean paused);
}
