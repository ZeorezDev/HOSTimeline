package com.hosserver.timeline.handler;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.TimelineEvent;
import com.hosserver.timeline.util.MessageUtil;
import com.hosserver.timeline.util.PlaceholderUtil;
import org.bukkit.Bukkit;

/**
 * Broadcasts a timeline event message to all online players (or a filtered subset).
 *
 * Processing order:
 *   1. Apply placeholders to the event's 'message' payload field
 *      ({date}, {year}, {month}, {day}, {event_id})
 *   2. Prepend event-specific 'prefix' (falls back to global prefix from config)
 *   3. Insert combined text into the global broadcast_format template via {message}
 *   4. Apply placeholders to the format result (so {year} also works in broadcast_format)
 *   5. Colorize and broadcast
 *
 * Required payload key : message  (String, supports placeholders)
 * Optional payload keys:
 *   prefix            (String) — overrides messages.prefix for this event only
 *   permission_filter (String) — only players with this permission receive the message
 *
 * Example YAML:
 *   type: BROADCAST
 *   payload:
 *     prefix: "&4[{year}] "
 *     message: "&cAlmanya Polonya'yı işgal etti!"
 *     permission_filter: "hostime.use"
 */
public final class BroadcastHandler implements EventHandler {

    private final HosTimeline plugin;

    public BroadcastHandler(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(TimelineEvent event) {
        Object rawMessage = event.getPayload().get("message");
        if (rawMessage == null) {
            plugin.getLogger().warning("BROADCAST event '" + event.getId()
                    + "' has no 'message' in payload. Skipping.");
            return;
        }

        TimelineDate date = plugin.getEngine().getCurrentDate();

        // ── Step 1: placeholders in the event message ────────────────────────
        String message = PlaceholderUtil.apply(rawMessage.toString(), date, event.getId());

        // ── Step 2: event prefix (with placeholders) ─────────────────────────
        Object rawPrefix = event.getPayload().get("prefix");
        String eventPrefix = rawPrefix != null
                ? PlaceholderUtil.apply(rawPrefix.toString(), date, event.getId())
                : plugin.getTimelineConfig().getPrefix();

        String combined = eventPrefix + message;

        // ── Step 3 + 4: wrap in global broadcast_format, then apply date placeholders
        String broadcastFormat = plugin.getTimelineConfig().getBroadcastFormat();
        String formatted = PlaceholderUtil.apply(
                broadcastFormat.replace("{message}", combined),
                date,
                event.getId()
        );

        // ── Step 5: colorize ─────────────────────────────────────────────────
        String permissionFilter = strOrNull(event, "permission_filter");
        String colorized = MessageUtil.colorize(formatted);

        // Dispatch on main thread (handler is already on main, runTask preserves ordering)
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (permissionFilter != null && !permissionFilter.isBlank()) {
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission(permissionFilter))
                        .forEach(p -> p.sendMessage(colorized));
                Bukkit.getConsoleSender().sendMessage(colorized);
            } else {
                Bukkit.broadcastMessage(colorized);
            }
        });
    }

    private String strOrNull(TimelineEvent event, String key) {
        Object v = event.getPayload().get(key);
        return v != null ? v.toString() : null;
    }
}
