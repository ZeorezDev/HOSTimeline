package com.hosserver.timeline.model;

import com.hosserver.timeline.api.TimelineDate;

/**
 * Immutable snapshot of a single fired timeline event.
 * Stored in event_log.yml.
 */
public final class EventLogEntry {

    private final String       eventId;
    private final EventType    eventType;
    private final TimelineDate gameDate;       // server date when the event fired
    private final String       realTimestamp;  // wall-clock time: "yyyy-MM-dd HH:mm:ss"

    public EventLogEntry(String eventId, EventType eventType,
                         TimelineDate gameDate, String realTimestamp) {
        this.eventId       = eventId;
        this.eventType     = eventType;
        this.gameDate      = gameDate;
        this.realTimestamp = realTimestamp;
    }

    public String       getEventId()       { return eventId; }
    public EventType    getEventType()     { return eventType; }
    public TimelineDate getGameDate()      { return gameDate; }
    public String       getRealTimestamp() { return realTimestamp; }

    @Override
    public String toString() {
        return realTimestamp + " | " + gameDate + " | " + eventId + " [" + eventType + "]";
    }
}
