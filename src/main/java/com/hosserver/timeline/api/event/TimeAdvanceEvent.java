package com.hosserver.timeline.api.event;

import com.hosserver.timeline.api.TimelineDate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired on the main thread whenever the server date changes.
 * Other plugins (Strategic Cities, Nation Traits, etc.) can listen to this
 * event without depending on HosTimeline internals.
 */
public class TimeAdvanceEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final TimelineDate oldDate;
    private final TimelineDate newDate;

    public TimeAdvanceEvent(TimelineDate oldDate, TimelineDate newDate) {
        this.oldDate = oldDate;
        this.newDate = newDate;
    }

    public TimelineDate getOldDate() { return oldDate; }
    public TimelineDate getNewDate() { return newDate; }

    /** True if the date moved forward; false if set to an earlier date. */
    public boolean isForward() {
        return newDate.compareTo(oldDate) > 0;
    }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
