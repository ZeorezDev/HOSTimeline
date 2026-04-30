package com.hosserver.timeline.config;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.ConditionOperator;
import com.hosserver.timeline.model.EventCondition;
import com.hosserver.timeline.model.EventType;
import com.hosserver.timeline.model.TimelineEvent;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * Loads and validates timeline_events.yml.
 *
 * Validation performed on load():
 *   - Duplicate event IDs → second occurrence skipped, warning logged
 *   - Missing required fields (id, date, type) → event skipped, warning logged
 *   - Unknown type string → event skipped, warning logged
 *   - Unparseable date → event skipped, warning logged
 *   - Event date outside configured year bounds → warning logged, event still loaded
 *   - Condition missing 'flag' → condition skipped, warning logged
 *   - EQUALS/NOT_EQUALS condition missing 'value' → condition skipped, warning logged
 *   - Events list empty → warning logged
 *
 * A complete validation summary is printed at INFO level after every load().
 */
public final class EventLoader {

    private final HosTimeline plugin;
    private final List<TimelineEvent> events = new ArrayList<>();

    public EventLoader(HosTimeline plugin) {
        this.plugin = plugin;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public void load() {
        events.clear();

        File file = new File(plugin.getDataFolder(), "timeline_events.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("[EventLoader] timeline_events.yml bulunamadı. Event yüklenmedi.");
            return;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> rawList = cfg.getMapList("events");

        if (rawList.isEmpty()) {
            plugin.getLogger().warning("[EventLoader] timeline_events.yml yüklendi ancak 'events' listesi boş veya eksik.");
            return;
        }

        int loaded  = 0;
        int skipped = 0;
        int warned  = 0;
        Set<String> seenIds = new LinkedHashSet<>();

        for (Map<?, ?> raw : rawList) {
            try {
                // ── Duplicate ID guard ───────────────────────────────────────
                String id = str(raw, "id");
                if (id != null && seenIds.contains(id)) {
                    plugin.getLogger().warning("[EventLoader] Tekrarlanan event id='" + id
                            + "'. İkinci tanım atlandı. Her id yalnızca bir kez tanımlanabilir.");
                    skipped++;
                    continue;
                }

                TimelineEvent event = parseEvent(raw);
                if (event == null) {
                    skipped++;
                    continue;
                }

                if (id != null) seenIds.add(id);

                // ── Date bounds advisory ─────────────────────────────────────
                int eventYear = event.getDate().getYear();
                int minYear   = plugin.getTimelineConfig().getMinYear();
                int maxYear   = plugin.getTimelineConfig().getMaxYear();
                if (eventYear < minYear || eventYear > maxYear) {
                    plugin.getLogger().warning("[EventLoader] Event '" + event.getId()
                            + "' tarihi (" + event.getDate() + ") yapılandırılmış yıl aralığı ["
                            + minYear + "-" + maxYear + "] dışında. Event yüklendi ancak "
                            + "güvenlik sınırı tarafından engellenebilir.");
                    warned++;
                }

                events.add(event);
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[EventLoader] Beklenmedik hata: " + raw, e);
                skipped++;
            }
        }

        events.sort(Comparator.comparing(TimelineEvent::getDate));

        // ── Summary ───────────────────────────────────────────────────────────
        plugin.getLogger().info("[EventLoader] Yüklendi: " + loaded
                + " event | Atlandı: " + skipped
                + " | Uyarı: " + warned
                + (loaded > 0 ? " | İlk: " + events.get(0).getDate()
                        + " | Son: " + events.get(events.size() - 1).getDate() : ""));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<TimelineEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /** Returns events in (from, to] window — strictly after from, on-or-before to. */
    public List<TimelineEvent> getEventsInRange(TimelineDate from, TimelineDate to) {
        List<TimelineEvent> result = new ArrayList<>();
        for (TimelineEvent e : events) {
            if (e.getDate().compareTo(from) > 0 && e.getDate().compareTo(to) <= 0) {
                result.add(e);
            }
        }
        return result;
    }

    // ── Parsing ───────────────────────────────────────────────────────────────

    private TimelineEvent parseEvent(Map<?, ?> raw) {
        String id      = str(raw, "id");
        String dateStr = str(raw, "date");
        String typeStr = str(raw, "type");

        if (id == null || id.isBlank()) {
            plugin.getLogger().warning("[EventLoader] 'id' alanı eksik bir event atlandı. Giriş: " + raw);
            return null;
        }
        if (dateStr == null) {
            plugin.getLogger().warning("[EventLoader] Event '" + id + "': 'date' alanı eksik. Event atlandı.");
            return null;
        }
        if (typeStr == null) {
            plugin.getLogger().warning("[EventLoader] Event '" + id + "': 'type' alanı eksik. Event atlandı.");
            return null;
        }

        TimelineDate date = parseDate(id, dateStr);
        if (date == null) return null;

        EventType type = EventType.fromString(typeStr);
        if (type == null) {
            plugin.getLogger().warning("[EventLoader] Event '" + id + "': bilinmeyen type='"
                    + typeStr + "'. Geçerli değerler: BROADCAST, CONSOLE_COMMAND, SET_FLAG. Event atlandı.");
            return null;
        }

        Object repeatableRaw = raw.get("repeatable");
        boolean repeatable   = (repeatableRaw instanceof Boolean) && (Boolean) repeatableRaw;

        // ── Payload ──────────────────────────────────────────────────────────
        Map<String, Object> payload = new HashMap<>();
        Object rawPayload = raw.get("payload");
        if (rawPayload instanceof Map<?, ?> pm) {
            pm.forEach((k, v) -> payload.put(k.toString(), v));
        } else if (rawPayload == null) {
            plugin.getLogger().warning("[EventLoader] Event '" + id + "': 'payload' alanı eksik. "
                    + "Bu event hiçbir şey yapmayabilir.");
        }

        // ── Type-specific payload validation ──────────────────────────────────
        validatePayload(id, type, payload);

        // ── Conditions ───────────────────────────────────────────────────────
        List<EventCondition> conditions = new ArrayList<>();
        Object rawConds = raw.get("conditions");
        if (rawConds instanceof List<?> condList) {
            for (Object condObj : condList) {
                EventCondition cond = parseCondition(id, condObj);
                if (cond != null) conditions.add(cond);
            }
        }

        return new TimelineEvent(id, date, type, repeatable, payload, conditions);
    }

    private void validatePayload(String id, EventType type, Map<String, Object> payload) {
        switch (type) {
            case BROADCAST -> {
                if (!payload.containsKey("message")) {
                    plugin.getLogger().warning("[EventLoader] Event '" + id
                            + "' (BROADCAST): payload'da 'message' eksik. Event yüklendi ama tetiklenince atlanacak.");
                }
            }
            case CONSOLE_COMMAND -> {
                Object cmds = payload.get("commands");
                if (cmds == null) {
                    plugin.getLogger().warning("[EventLoader] Event '" + id
                            + "' (CONSOLE_COMMAND): payload'da 'commands' listesi eksik.");
                } else if (cmds instanceof List<?> list && list.isEmpty()) {
                    plugin.getLogger().warning("[EventLoader] Event '" + id
                            + "' (CONSOLE_COMMAND): 'commands' listesi boş.");
                }
            }
            case SET_FLAG -> {
                if (!payload.containsKey("flag")) {
                    plugin.getLogger().warning("[EventLoader] Event '" + id
                            + "' (SET_FLAG): payload'da 'flag' eksik.");
                }
                if (!payload.containsKey("value")) {
                    plugin.getLogger().warning("[EventLoader] Event '" + id
                            + "' (SET_FLAG): payload'da 'value' eksik.");
                }
            }
        }
    }

    private EventCondition parseCondition(String eventId, Object condObj) {
        if (!(condObj instanceof Map<?, ?> condMap)) return null;

        String flag    = str(condMap, "flag");
        String value   = str(condMap, "value");
        String opStr   = str(condMap, "operator");
        ConditionOperator op = ConditionOperator.fromString(opStr);

        if (flag == null || flag.isBlank()) {
            plugin.getLogger().warning("[EventLoader] Event '" + eventId
                    + "': koşulda 'flag' alanı eksik. Koşul atlandı.");
            return null;
        }

        if (value == null && (op == ConditionOperator.EQUALS || op == ConditionOperator.NOT_EQUALS)) {
            plugin.getLogger().warning("[EventLoader] Event '" + eventId
                    + "': '" + flag + "' bayrağı için " + op + " operatörü 'value' gerektiriyor. Koşul atlandı.");
            return null;
        }

        return new EventCondition(flag, value != null ? value : "", op);
    }

    private TimelineDate parseDate(String eventId, String raw) {
        String[] parts = raw.split("-");
        if (parts.length != 3) {
            plugin.getLogger().warning("[EventLoader] Event '" + eventId
                    + "': geçersiz tarih formatı '" + raw + "'. Beklenen: YYYY-MM-DD. Event atlandı.");
            return null;
        }
        try {
            return new TimelineDate(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[EventLoader] Event '" + eventId
                    + "': tarih ayrıştırılamadı '" + raw + "': " + e.getMessage() + ". Event atlandı.");
            return null;
        }
    }

    private static String str(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
