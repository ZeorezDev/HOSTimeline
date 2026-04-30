package com.hosserver.timeline.handler;

import com.hosserver.timeline.model.TimelineEvent;

/** Strategy interface for processing a fired timeline event. */
public interface EventHandler {
    void handle(TimelineEvent event);
}
