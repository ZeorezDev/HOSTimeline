package com.hosserver.timeline.handler;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.model.EventType;

import java.util.EnumMap;
import java.util.Map;

/** Maps EventType → EventHandler. External plugins can register custom handlers. */
public final class EventHandlerRegistry {

    private final Map<EventType, EventHandler> handlers = new EnumMap<>(EventType.class);

    public EventHandlerRegistry(HosTimeline plugin) {
        handlers.put(EventType.BROADCAST,       new BroadcastHandler(plugin));
        handlers.put(EventType.CONSOLE_COMMAND,  new ConsoleCommandHandler(plugin));
        handlers.put(EventType.SET_FLAG,         new SetFlagHandler(plugin));
    }

    /**
     * Retrieve the handler for the given type.
     * @throws IllegalStateException if no handler is registered (should never happen for built-in types).
     */
    public EventHandler getHandler(EventType type) {
        EventHandler h = handlers.get(type);
        if (h == null) {
            throw new IllegalStateException("No handler registered for EventType: " + type);
        }
        return h;
    }

    /** Register or replace a handler. Useful for other plugins adding custom event types via reflection. */
    public void register(EventType type, EventHandler handler) {
        handlers.put(type, handler);
    }
}
