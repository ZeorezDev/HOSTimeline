package com.hosserver.timeline.engine;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.event.TimeAdvanceEvent;
import com.hosserver.timeline.api.event.TimelineEventFireEvent;
import com.hosserver.timeline.model.EventCondition;
import com.hosserver.timeline.model.FlagStore;
import com.hosserver.timeline.model.TimelineEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Listens to {@link TimeAdvanceEvent} and orchestrates the full event pipeline
 * for every timeline event that falls in the advanced date window.
 *
 * Per-event pipeline:
 *   1. Skip if already fired (non-repeatable guard)
 *   2. Evaluate conditions (all must pass — AND logic)
 *   3. Fire cancellable {@link TimelineEventFireEvent} — other plugins may cancel here
 *   4. Dispatch to the registered handler (BroadcastHandler / ConsoleCommandHandler / SetFlagHandler)
 *   5. Mark fired in storage (non-repeatable only)
 *   6. Record in EventLog
 *   7. Notify registered {@link com.hosserver.timeline.api.integration.TimelineIntegration} instances
 *
 * Steps 4–7 are all skipped when the TimelineEventFireEvent is cancelled.
 *
 * Date-change notifications:
 *   IntegrationRegistry.notifyDateChanged() is called once at the top of onTimeAdvance,
 *   before any event processing, so integrations always know the new date first.
 */
public final class EventScheduler implements Listener {

    private final HosTimeline plugin;

    public EventScheduler(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTimeAdvance(TimeAdvanceEvent event) {
        if (!event.isForward()) return;

        // ── Notify integrations of date change (always, even with no events) ─
        plugin.getIntegrationRegistry()
              .notifyDateChanged(event.getOldDate(), event.getNewDate());

        List<TimelineEvent> candidates = plugin.getEventLoader()
                .getEventsInRange(event.getOldDate(), event.getNewDate());
        if (candidates.isEmpty()) return;

        FlagStore flagStore = plugin.getStorageManager().getFlagStore();

        for (TimelineEvent te : candidates) {

            // ── 1. Non-repeatable guard ──────────────────────────────────────
            if (!te.isRepeatable()
                    && plugin.getStorageManager().getFiredEvents().contains(te.getId())) {
                plugin.getLogger().fine("[Timeline] Skipping already-fired: " + te.getId());
                continue;
            }

            // ── 2. Condition check ───────────────────────────────────────────
            if (!conditionsMet(te, flagStore)) continue;

            // ── 3. Cancellable Bukkit event ──────────────────────────────────
            TimelineEventFireEvent fireEvent = new TimelineEventFireEvent(te, event.getNewDate());
            plugin.getServer().getPluginManager().callEvent(fireEvent);

            if (fireEvent.isCancelled()) {
                plugin.getLogger().info("[Timeline] Event '" + te.getId()
                        + "' was cancelled by an external plugin.");
                continue;
            }

            // ── 4. Dispatch to handler ───────────────────────────────────────
            dispatchEvent(te);

            // ── 5. Mark fired ────────────────────────────────────────────────
            if (!te.isRepeatable()) {
                plugin.getStorageManager().addFiredEvent(te.getId());
                plugin.getStorageManager().save();
            }

            // ── 6. Log ───────────────────────────────────────────────────────
            plugin.getEventLog().record(te);

            // ── 7. Notify integrations ───────────────────────────────────────
            plugin.getIntegrationRegistry().notifyEventFired(te, event.getNewDate());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean conditionsMet(TimelineEvent te, FlagStore flagStore) {
        List<EventCondition> conditions = te.getConditions();
        if (conditions.isEmpty()) return true;
        for (EventCondition c : conditions) {
            if (!c.evaluate(flagStore)) {
                plugin.getLogger().fine("[Timeline] Event '" + te.getId()
                        + "' skipped — condition not met: " + c);
                return false;
            }
        }
        return true;
    }

    private void dispatchEvent(TimelineEvent te) {
        try {
            plugin.getHandlerRegistry().getHandler(te.getType()).handle(te);
        } catch (Exception e) {
            plugin.getLogger().warning("[Timeline] Error dispatching event '"
                    + te.getId() + "': " + e.getMessage());
        }
    }
}
