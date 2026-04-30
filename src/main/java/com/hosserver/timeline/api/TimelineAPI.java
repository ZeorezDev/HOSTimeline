package com.hosserver.timeline.api;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.integration.IntegrationRegistry;

import java.util.Set;

/**
 * Public facade for other plugins.
 * Depend only on this class and the api.* / api.integration.* / api.event.* packages.
 * Never import from engine, handler, config, storage, or command packages.
 *
 * ── Soft-depend pattern ────────────────────────────────────────────────────
 *
 *   // plugin.yml:
 *   softdepend: [HosTimeline]
 *
 *   // onEnable:
 *   if (TimelineAPI.isAvailable()) {
 *       int year = TimelineAPI.getCurrentYear();
 *       TimelineAPI.getIntegrationRegistry().register(new MyIntegration(this));
 *   }
 *
 * ── Bukkit event listening (no registration required) ─────────────────────
 *
 *   @EventHandler
 *   public void onTimelineEvent(TimelineEventFireEvent e) {
 *       // Runs when any timeline event fires. Cancel to suppress it.
 *   }
 *
 *   @EventHandler
 *   public void onDateChange(TimeAdvanceEvent e) {
 *       // Runs on every date change, forward or backward.
 *   }
 */
public final class TimelineAPI {

    private static HosTimeline plugin;

    private TimelineAPI() {}

    /** Called once by HosTimeline during onEnable. Not part of the public API. */
    public static void init(HosTimeline instance) {
        plugin = instance;
    }

    /** Returns true when HosTimeline is loaded and enabled. Always check this before calling other methods. */
    public static boolean isAvailable() {
        return plugin != null && plugin.isEnabled();
    }

    // ── Date ──────────────────────────────────────────────────────────────────

    /** Returns the current server date, or {@code null} if unavailable. */
    public static TimelineDate getCurrentDate() {
        if (!isAvailable()) return null;
        return plugin.getEngine().getCurrentDate();
    }

    /** Returns the current year, or {@code -1} if unavailable. */
    public static int getCurrentYear() {
        TimelineDate d = getCurrentDate();
        return d != null ? d.getYear() : -1;
    }

    // ── Flags ─────────────────────────────────────────────────────────────────

    /** Returns the value of a flag, or {@code null} if not set or unavailable. */
    public static String getFlag(String key) {
        if (!isAvailable()) return null;
        return plugin.getStorageManager().getFlagStore().get(key);
    }

    /** Returns {@code true} if the flag exists and equals the given value. */
    public static boolean flagMatches(String key, String value) {
        if (!isAvailable()) return false;
        return plugin.getStorageManager().getFlagStore().matches(key, value);
    }

    /** Returns {@code true} if the flag key exists (any value). */
    public static boolean flagExists(String key) {
        if (!isAvailable()) return false;
        return plugin.getStorageManager().getFlagStore().has(key);
    }

    // ── Event history ─────────────────────────────────────────────────────────

    /** Returns {@code true} if the given event ID has already been fired. */
    public static boolean isEventFired(String eventId) {
        if (!isAvailable()) return false;
        return plugin.getStorageManager().getFiredEvents().contains(eventId);
    }

    /** Returns an unmodifiable view of all fired event IDs. */
    public static Set<String> getFiredEvents() {
        if (!isAvailable()) return Set.of();
        return plugin.getStorageManager().getFiredEvents();
    }

    // ── Integration ───────────────────────────────────────────────────────────

    /**
     * Returns the {@link IntegrationRegistry} for registering deep integrations,
     * or {@code null} if HosTimeline is unavailable.
     *
     * Always guard with {@link #isAvailable()} before calling.
     *
     * Example:
     *   IntegrationRegistry reg = TimelineAPI.getIntegrationRegistry();
     *   if (reg != null) reg.register(new MyIntegration(this));
     */
    public static IntegrationRegistry getIntegrationRegistry() {
        if (!isAvailable()) return null;
        return plugin.getIntegrationRegistry();
    }
}
