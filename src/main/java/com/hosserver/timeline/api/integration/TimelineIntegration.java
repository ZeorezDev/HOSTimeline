package com.hosserver.timeline.api.integration;

import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.TimelineEvent;

/**
 * SPI (Service Provider Interface) for plugins that need deep, structured
 * integration with HOS Timeline without relying solely on Bukkit event listeners.
 *
 * All methods have default no-op implementations — implementors override only
 * what they need. Adding new callbacks here is non-breaking for existing integrations.
 *
 * ── Registration ───────────────────────────────────────────────────────────
 *
 *   // In your plugin's onEnable, after HosTimeline has loaded:
 *   IntegrationRegistry reg = TimelineAPI.getIntegrationRegistry();
 *   if (reg != null) {
 *       reg.register(new MyPluginTimelineIntegration(this));
 *   }
 *
 * ── De-registration ────────────────────────────────────────────────────────
 *
 *   // In your plugin's onDisable:
 *   IntegrationRegistry reg = TimelineAPI.getIntegrationRegistry();
 *   if (reg != null) {
 *       reg.unregister(getIntegrationName());
 *   }
 *
 * ── Strategic Cities example ───────────────────────────────────────────────
 *
 *   public class StrategicCitiesIntegration implements TimelineIntegration {
 *       private final StrategicCitiesPlugin plugin;
 *
 *       @Override public String getIntegrationName() { return "StrategicCities"; }
 *
 *       @Override
 *       public void onEventFired(TimelineEvent event, TimelineDate date) {
 *           if ("poland_fallen_flag".equals(event.getId())) {
 *               plugin.getCityManager().setOccupied("Warsaw");
 *           }
 *       }
 *
 *       @Override
 *       public void onDateChanged(TimelineDate oldDate, TimelineDate newDate) {
 *           if (oldDate.getYear() != newDate.getYear()) {
 *               plugin.getProductionManager().recalcYearlyBonuses(newDate.getYear());
 *           }
 *       }
 *   }
 *
 * ── Nation Traits example ──────────────────────────────────────────────────
 *
 *   public class NationTraitsIntegration implements TimelineIntegration {
 *       @Override public String getIntegrationName() { return "NationTraits"; }
 *
 *       @Override
 *       public void onFlagChanged(String flag, String value, String sourceEventId) {
 *           if ("ww2_active".equals(flag) && "true".equals(value)) {
 *               traitManager.applyWarTraits();
 *           }
 *       }
 *   }
 */
public interface TimelineIntegration {

    /**
     * Human-readable identifier for this integration. Used in logs and
     * for de-registration by name. Must be unique across registered integrations.
     */
    String getIntegrationName();

    /**
     * Called after a timeline event has been successfully dispatched.
     * Not called if the event was cancelled via {@code TimelineEventFireEvent}.
     *
     * @param event    the fired timeline event (id, type, date, payload)
     * @param gameDate the server date at the time of firing
     */
    default void onEventFired(TimelineEvent event, TimelineDate gameDate) {}

    /**
     * Called after the server date changes (forward or backward).
     * Fires even if no timeline events were triggered by the advance.
     *
     * @param oldDate the date before the change
     * @param newDate the date after the change
     */
    default void onDateChanged(TimelineDate oldDate, TimelineDate newDate) {}

    /**
     * Called when a SET_FLAG event successfully sets a flag.
     * Fired after the flag is persisted to storage.
     *
     * @param flag         the flag key (lower-case normalised)
     * @param value        the new flag value
     * @param sourceEventId the id of the SET_FLAG event that triggered this, or null if set manually
     */
    default void onFlagChanged(String flag, String value, String sourceEventId) {}
}
