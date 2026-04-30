package com.hosserver.timeline.model;

import com.hosserver.timeline.api.TimelineDate;

import java.util.List;
import java.util.Map;

/** Immutable representation of a single configured timeline event. */
public final class TimelineEvent {

    private final String id;
    private final TimelineDate date;
    private final EventType type;
    private final boolean repeatable;
    private final Map<String, Object> payload;
    private final List<EventCondition> conditions;

    public TimelineEvent(
            String id,
            TimelineDate date,
            EventType type,
            boolean repeatable,
            Map<String, Object> payload,
            List<EventCondition> conditions
    ) {
        this.id         = id;
        this.date       = date;
        this.type       = type;
        this.repeatable = repeatable;
        this.payload    = Map.copyOf(payload);
        this.conditions = List.copyOf(conditions);
    }

    public String getId()                    { return id; }
    public TimelineDate getDate()            { return date; }
    public EventType getType()               { return type; }
    public boolean isRepeatable()            { return repeatable; }
    public Map<String, Object> getPayload()  { return payload; }
    public List<EventCondition> getConditions() { return conditions; }

    @Override
    public String toString() {
        return "TimelineEvent{id='" + id + "', date=" + date + ", type=" + type + "}";
    }
}
