package com.hosserver.timeline.api.integration;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.TimelineEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds all registered {@link TimelineIntegration} instances and fans out
 * notifications to them in registration order.
 *
 * All methods are main-thread only (same constraint as the rest of the plugin).
 * Exceptions thrown by an integration are caught and logged — one bad integration
 * never silences the others.
 *
 * Accessed via {@code TimelineAPI.getIntegrationRegistry()}.
 */
public final class IntegrationRegistry {

    private final HosTimeline plugin;

    // ArrayList is fine — registrations happen once at startup, notifications are read-only
    private final List<TimelineIntegration> integrations = new ArrayList<>();

    public IntegrationRegistry(HosTimeline plugin) {
        this.plugin = plugin;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Register an integration. Safe to call multiple times with different instances.
     * Duplicate names are allowed but will produce a warning.
     */
    public void register(TimelineIntegration integration) {
        if (integration == null) throw new IllegalArgumentException("Integration must not be null");

        boolean duplicate = integrations.stream()
                .anyMatch(i -> i.getIntegrationName().equals(integration.getIntegrationName()));
        if (duplicate) {
            plugin.getLogger().warning("[Timeline] Duplicate integration name registered: '"
                    + integration.getIntegrationName() + "'. Both will receive callbacks.");
        }

        integrations.add(integration);
        plugin.getLogger().info("[Timeline] Integration registered: " + integration.getIntegrationName()
                + " (total: " + integrations.size() + ")");
    }

    /**
     * Unregister all integrations with the given name.
     * Call this from the integrating plugin's onDisable.
     */
    public void unregister(String integrationName) {
        int before = integrations.size();
        integrations.removeIf(i -> i.getIntegrationName().equals(integrationName));
        int removed = before - integrations.size();
        if (removed > 0) {
            plugin.getLogger().info("[Timeline] Integration unregistered: " + integrationName);
        }
    }

    /** Returns an unmodifiable snapshot of all currently registered integrations. */
    public List<TimelineIntegration> getAll() {
        return Collections.unmodifiableList(integrations);
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    /**
     * Notify all integrations that a timeline event has fired.
     * Iterates over a snapshot copy so registrations/unregistrations mid-callback are safe.
     */
    public void notifyEventFired(TimelineEvent event, TimelineDate gameDate) {
        for (TimelineIntegration integration : snapshot()) {
            try {
                integration.onEventFired(event, gameDate);
            } catch (Exception e) {
                plugin.getLogger().warning("[Timeline] Integration '" + integration.getIntegrationName()
                        + "' threw an exception in onEventFired: " + e.getMessage());
            }
        }
    }

    /**
     * Notify all integrations that the server date has changed.
     * Called even when no timeline events were triggered by the advance.
     */
    public void notifyDateChanged(TimelineDate oldDate, TimelineDate newDate) {
        for (TimelineIntegration integration : snapshot()) {
            try {
                integration.onDateChanged(oldDate, newDate);
            } catch (Exception e) {
                plugin.getLogger().warning("[Timeline] Integration '" + integration.getIntegrationName()
                        + "' threw an exception in onDateChanged: " + e.getMessage());
            }
        }
    }

    /**
     * Notify all integrations that a flag was set.
     * Called by SetFlagHandler after the flag is persisted.
     *
     * @param sourceEventId the id of the SET_FLAG event, or null if set via command/API
     */
    public void notifyFlagChanged(String flag, String value, String sourceEventId) {
        for (TimelineIntegration integration : snapshot()) {
            try {
                integration.onFlagChanged(flag, value, sourceEventId);
            } catch (Exception e) {
                plugin.getLogger().warning("[Timeline] Integration '" + integration.getIntegrationName()
                        + "' threw an exception in onFlagChanged: " + e.getMessage());
            }
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Returns a defensive copy so callers iterating the list won't see concurrent modification. */
    private List<TimelineIntegration> snapshot() {
        return new ArrayList<>(integrations);
    }
}
