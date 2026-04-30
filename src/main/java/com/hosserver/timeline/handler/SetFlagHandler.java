package com.hosserver.timeline.handler;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.model.TimelineEvent;

/**
 * Sets a key-value flag in the persistent FlagStore.
 *
 * Required payload keys: flag (String), value (String)
 *
 * After persisting, notifies registered integrations via
 * IntegrationRegistry.notifyFlagChanged() so plugins like Nation Traits
 * can react to specific flag transitions without polling.
 */
public final class SetFlagHandler implements EventHandler {

    private final HosTimeline plugin;

    public SetFlagHandler(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(TimelineEvent event) {
        Object rawFlag  = event.getPayload().get("flag");
        Object rawValue = event.getPayload().get("value");

        if (rawFlag == null || rawValue == null) {
            plugin.getLogger().warning("SET_FLAG event '" + event.getId()
                    + "' missing 'flag' or 'value' in payload. Skipping.");
            return;
        }

        String flag  = rawFlag.toString();
        String value = rawValue.toString();

        // Persist immediately — flags survive restarts
        plugin.getStorageManager().getFlagStore().set(flag, value);
        plugin.getStorageManager().save();

        plugin.getLogger().info("[Timeline] Flag set: " + flag + " = " + value
                + "  (event: " + event.getId() + ")");

        // Notify integrations — sourceEventId carried so they can filter by event
        plugin.getIntegrationRegistry().notifyFlagChanged(flag, value, event.getId());
    }
}
