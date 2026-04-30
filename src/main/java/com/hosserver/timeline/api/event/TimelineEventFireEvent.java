package com.hosserver.timeline.api.event;

import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.TimelineEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired on the main thread immediately before a timeline event is dispatched.
 *
 * Other plugins can listen to this event to:
 *   - React to specific event IDs or types
 *   - Cancel the event (prevents dispatch, fired_events mark, and log entry)
 *   - Read current flag context via TimelineAPI
 *
 * ── Soft-depend usage example (in another plugin) ──────────────────────────
 *
 *   // plugin.yml:  softdepend: [HosTimeline]
 *
 *   @EventHandler
 *   public void onTimelineEvent(TimelineEventFireEvent e) {
 *       if ("ww2_start".equals(e.getTimelineEvent().getId())) {
 *           myPlugin.activateWarMode();
 *       }
 *   }
 *
 * ── Cancel example ─────────────────────────────────────────────────────────
 *
 *   @EventHandler
 *   public void onTimelineEvent(TimelineEventFireEvent e) {
 *       if ("poland_fallen_broadcast".equals(e.getTimelineEvent().getId())
 *               && myPlugin.isPeaceTreatyActive()) {
 *           e.setCancelled(true);   // suppress the broadcast in this scenario
 *       }
 *   }
 */
public final class TimelineEventFireEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final TimelineEvent timelineEvent;
    private final TimelineDate  gameDate;
    private boolean             cancelled = false;

    public TimelineEventFireEvent(TimelineEvent timelineEvent, TimelineDate gameDate) {
        this.timelineEvent = timelineEvent;
        this.gameDate      = gameDate;
    }

    // ── Payload ───────────────────────────────────────────────────────────────

    /** The timeline event that is about to be dispatched. */
    public TimelineEvent getTimelineEvent() { return timelineEvent; }

    /**
     * The current server date at the moment of dispatch.
     * For bulk advances this equals the event's own date, not the final date.
     */
    public TimelineDate getGameDate() { return gameDate; }

    // ── Cancellable ───────────────────────────────────────────────────────────

    @Override public boolean isCancelled()          { return cancelled; }
    @Override public void    setCancelled(boolean c) { this.cancelled = c; }

    // ── Bukkit boilerplate ────────────────────────────────────────────────────

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
