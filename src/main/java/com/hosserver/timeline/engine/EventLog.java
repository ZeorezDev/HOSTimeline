package com.hosserver.timeline.engine;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.EventLogEntry;
import com.hosserver.timeline.model.EventType;
import com.hosserver.timeline.model.TimelineEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

/**
 * Persistent, append-only log of every fired timeline event.
 *
 * Storage: plugins/HosTimeline/event_log.yml
 * Format:
 *   entries:
 *     - event_id:  ww2_start
 *       type:      BROADCAST
 *       game_date: "1939-09-01"
 *       real_time: "2024-09-01 14:23:05"
 *
 * Capped at MAX_ENTRIES (oldest entries removed first when exceeded).
 */
public final class EventLog {

    public static final int PAGE_SIZE = 8;
    private static final int MAX_ENTRIES = 500;
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HosTimeline plugin;
    private final File file;
    private final List<EventLogEntry> entries = new ArrayList<>();

    public EventLog(HosTimeline plugin) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), "event_log.yml");
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void load() {
        entries.clear();
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> rawList = cfg.getMapList("entries");

        for (Map<?, ?> raw : rawList) {
            try {
                String eventId   = str(raw, "event_id");
                String typeStr   = str(raw, "type");
                String dateStr   = str(raw, "game_date");
                String timestamp = str(raw, "real_time");

                if (eventId == null || typeStr == null || dateStr == null || timestamp == null) continue;

                String[] parts = dateStr.split("-");
                if (parts.length != 3) continue;

                TimelineDate date = new TimelineDate(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                );
                EventType type = EventType.fromString(typeStr);
                if (type == null) type = EventType.BROADCAST;

                entries.add(new EventLogEntry(eventId, type, date, timestamp));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Skipping malformed event_log entry: " + raw, e);
            }
        }

        plugin.getLogger().info("Event log loaded: " + entries.size()
                + " entr" + (entries.size() == 1 ? "y" : "ies") + ".");
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        List<Map<String, String>> rawList = new ArrayList<>();

        for (EventLogEntry e : entries) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("event_id",  e.getEventId());
            map.put("type",      e.getEventType().name());
            map.put("game_date", e.getGameDate().toString());
            map.put("real_time", e.getRealTimestamp());
            rawList.add(map);
        }

        cfg.set("entries", rawList);
        try {
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save event_log.yml", ex);
        }
    }

    // ── Record ────────────────────────────────────────────────────────────────

    /**
     * Appends a fired event to the log and flushes to disk immediately.
     * Uses the event's own date — not the engine's current date — so that
     * bulk advances (e.g. advance 365) log each event at its correct date.
     */
    public void record(TimelineEvent event) {
        String timestamp = LocalDateTime.now().format(TIME_FMT);
        entries.add(new EventLogEntry(event.getId(), event.getType(),
                event.getDate(), timestamp));

        // Evict oldest entries beyond the cap
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }

        save();

        plugin.getLogger().info("[Timeline] LOG: " + event.getId()
                + " @ " + event.getDate().format()
                + " (" + event.getType() + ")  real=" + timestamp);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns a page of entries in reverse chronological order (newest first).
     * Page numbers are 1-based.
     */
    public List<EventLogEntry> getPage(int page, int pageSize) {
        if (entries.isEmpty()) return Collections.emptyList();
        int ps = Math.max(1, pageSize);

        // Reverse so index 0 = newest
        List<EventLogEntry> reversed = new ArrayList<>(entries);
        Collections.reverse(reversed);

        int from = (page - 1) * ps;
        if (from >= reversed.size()) return Collections.emptyList();
        int to = Math.min(from + ps, reversed.size());
        return Collections.unmodifiableList(reversed.subList(from, to));
    }

    public int getTotalPages(int pageSize) {
        if (entries.isEmpty()) return 0;
        return (int) Math.ceil((double) entries.size() / Math.max(1, pageSize));
    }

    public int getTotalEntries() { return entries.size(); }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static String str(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
