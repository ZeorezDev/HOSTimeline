package com.hosserver.timeline.config;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed, validated wrapper around config.yml.
 * Re-created on /hostime reload — all fields are final.
 *
 * Validation rules:
 *   - start_date : invalid month/day → reset to 1936-01-01, warning logged
 *   - interval_ticks < 20            → clamped to 20, warning logged
 *   - days_per_advance < 1           → clamped to 1, warning logged
 *   - max_advance_days < 0           → clamped to 0, warning logged
 *   - min_year >= max_year           → reset to defaults (1900/2100), warning logged
 *   - broadcast_format missing {message} → warning logged (broadcasts will appear blank)
 */
public final class TimelineConfig {

    private final TimelineDate startDate;
    private final boolean      autoAdvanceEnabled;
    private final long         autoAdvanceIntervalTicks;
    private final int          autoAdvanceDays;
    private final int          maxAdvanceDays;
    private final int          minYear;
    private final int          maxYear;
    private final String       prefix;
    private final String       broadcastFormat;

    public TimelineConfig(HosTimeline plugin) {
        FileConfiguration cfg = plugin.getConfig();

        // ── start_date ────────────────────────────────────────────────────────
        int year  = cfg.getInt("timeline.start_date.year",  1936);
        int month = cfg.getInt("timeline.start_date.month", 1);
        int day   = cfg.getInt("timeline.start_date.day",   1);
        TimelineDate parsed;
        try {
            parsed = new TimelineDate(year, month, day);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Config] timeline.start_date geçersiz ("
                    + e.getMessage() + "). 1936-01-01 kullanılıyor.");
            parsed = new TimelineDate(1936, 1, 1);
        }
        this.startDate = parsed;

        // ── auto_advance ──────────────────────────────────────────────────────
        this.autoAdvanceEnabled = cfg.getBoolean("timeline.auto_advance.enabled", false);

        long interval = cfg.getLong("timeline.auto_advance.interval_ticks", 72000L);
        if (interval < 20) {
            plugin.getLogger().warning("[Config] auto_advance.interval_ticks=" + interval
                    + " çok düşük (< 20 tick). 20'ye sabitlendi.");
            interval = 20;
        }
        this.autoAdvanceIntervalTicks = interval;

        int advDays = cfg.getInt("timeline.auto_advance.days_per_advance", 1);
        if (advDays < 1) {
            plugin.getLogger().warning("[Config] auto_advance.days_per_advance=" + advDays
                    + " geçersiz. 1'e sabitlendi.");
            advDays = 1;
        }
        this.autoAdvanceDays = advDays;

        // ── safety ────────────────────────────────────────────────────────────
        int maxAdv = cfg.getInt("timeline.safety.max_advance_days", 3650);
        if (maxAdv < 0) {
            plugin.getLogger().warning("[Config] safety.max_advance_days=" + maxAdv
                    + " negatif olamaz. 0 (sınırsız) kullanılıyor.");
            maxAdv = 0;
        }
        this.maxAdvanceDays = maxAdv;

        int minYr = cfg.getInt("timeline.safety.min_year", 1900);
        int maxYr = cfg.getInt("timeline.safety.max_year", 2100);
        if (minYr >= maxYr) {
            plugin.getLogger().warning("[Config] safety.min_year (" + minYr
                    + ") >= max_year (" + maxYr + "). Varsayılanlara dönülüyor: 1900-2100.");
            minYr = 1900;
            maxYr = 2100;
        }
        this.minYear = minYr;
        this.maxYear = maxYr;

        if (startDate.getYear() < minYear || startDate.getYear() > maxYear) {
            plugin.getLogger().warning("[Config] start_date yılı (" + startDate.getYear()
                    + ") güvenli yıl aralığı dışında [" + minYear + "-" + maxYear + "].");
        }

        // ── messages ──────────────────────────────────────────────────────────
        this.prefix = cfg.getString("messages.prefix", "&8[&6HOS Timeline&8] &r");

        String fmt = cfg.getString("messages.broadcast_format", "{message}");
        if (!fmt.contains("{message}")) {
            plugin.getLogger().warning("[Config] messages.broadcast_format içinde {message} yok. "
                    + "Broadcast mesajları boş görünebilir. Varsayılana dönülüyor.");
            fmt = "{message}";
        }
        this.broadcastFormat = fmt;

        // ── startup summary ───────────────────────────────────────────────────
        plugin.getLogger().info("[Config] Başlangıç tarihi: " + startDate
                + " | Yıl aralığı: [" + minYear + "-" + maxYear + "]"
                + " | Maks. ilerleme: " + (maxAdvanceDays == 0 ? "sınırsız" : maxAdvanceDays + " gün")
                + " | Otomatik: " + (autoAdvanceEnabled
                        ? "AÇIK (" + autoAdvanceIntervalTicks + " tick, +" + autoAdvanceDays + " gün)"
                        : "KAPALI"));
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public TimelineDate getStartDate()               { return startDate; }
    public boolean      isAutoAdvanceEnabled()        { return autoAdvanceEnabled; }
    public long         getAutoAdvanceIntervalTicks() { return autoAdvanceIntervalTicks; }
    public int          getAutoAdvanceDays()          { return autoAdvanceDays; }
    /** Maximum days allowed per /hostime advance. 0 = no limit. */
    public int          getMaxAdvanceDays()           { return maxAdvanceDays; }
    public int          getMinYear()                  { return minYear; }
    public int          getMaxYear()                  { return maxYear; }
    public String       getPrefix()                   { return prefix; }
    public String       getBroadcastFormat()          { return broadcastFormat; }
}
