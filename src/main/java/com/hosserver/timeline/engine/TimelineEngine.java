package com.hosserver.timeline.engine;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.api.event.TimeAdvanceEvent;

/**
 * Core date management engine.
 * All mutation goes through this class so every caller fires the same Bukkit event.
 */
public final class TimelineEngine {

    private final HosTimeline plugin;

    private TimelineDate currentDate;
    private boolean paused;

    public TimelineEngine(HosTimeline plugin) {
        this.plugin = plugin;
    }

    /** Sync engine state from storage after storage has been loaded. */
    public void loadState() {
        this.currentDate = plugin.getStorageManager().getCurrentDate();
        this.paused      = plugin.getStorageManager().isPaused();
    }

    // ── Date mutations ────────────────────────────────────────────────────────

    /**
     * Advance the date by {@code days} days.
     * Fires {@link TimeAdvanceEvent}. Persists state.
     */
    public void advance(int days) {
        TimelineDate newDate = currentDate.plusDays(days);
        applyDateChange(newDate);
    }

    /**
     * Jump to a specific date.
     * Fires {@link TimeAdvanceEvent}. Persists state.
     * If {@code newDate} is earlier than the current date, events will NOT fire
     * (TimeAdvanceEvent.isForward() == false).
     */
    public void setDate(TimelineDate newDate) {
        applyDateChange(newDate);
    }

    private void applyDateChange(TimelineDate newDate) {
        TimelineDate oldDate = this.currentDate;
        this.currentDate = newDate;

        plugin.getStorageManager().setCurrentDate(newDate);
        plugin.getStorageManager().save();

        // Notify EventScheduler and any external listeners
        plugin.getServer().getPluginManager().callEvent(new TimeAdvanceEvent(oldDate, newDate));
    }

    // ── Pause / resume ────────────────────────────────────────────────────────

    public void pause() {
        paused = true;
        plugin.getStorageManager().setPaused(true);
        plugin.getStorageManager().save();
    }

    public void resume() {
        paused = false;
        plugin.getStorageManager().setPaused(false);
        plugin.getStorageManager().save();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public TimelineDate getCurrentDate() { return currentDate; }
    public boolean isPaused()            { return paused; }
}
