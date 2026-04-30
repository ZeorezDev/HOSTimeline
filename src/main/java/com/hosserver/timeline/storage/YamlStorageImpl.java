package com.hosserver.timeline.storage;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.model.FlagStore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

/**
 * Persists timeline state to plugins/HosTimeline/server_date.yml.
 *
 * ── Atomik yazım ──────────────────────────────────────────────────────────────
 * save() doğrudan server_date.yml üzerine yazmaz. Önce .tmp dosyasına yazar,
 * başarılı olursa .yml'i .bak'a yedekler, ardından .tmp'yi .yml'e taşır.
 * Sunucu çökmesi durumunda en kötü senaryo: .bak dosyasından kurtarma.
 *
 * ── Yedekten kurtarma ─────────────────────────────────────────────────────────
 * Eğer server_date.yml eksikse veya okunamıyorsa ve .bak mevcutsa,
 * load() otomatik olarak .bak dosyasını kullanır.
 *
 * ── Yıl sınırı kontrolü ───────────────────────────────────────────────────────
 * Yüklenen tarih config.yml'deki [min_year, max_year] aralığı dışındaysa
 * start_date'e sıfırlanır ve kaydedilir.
 */
public class YamlStorageImpl implements StorageManager {

    private final HosTimeline plugin;
    private final File file;
    private final File tmpFile;
    private final File bakFile;

    private TimelineDate currentDate;
    private final Set<String> firedEvents = new HashSet<>();
    private final FlagStore   flagStore   = new FlagStore();
    private boolean paused = false;

    public YamlStorageImpl(HosTimeline plugin) {
        this.plugin  = plugin;
        this.file    = new File(plugin.getDataFolder(), "server_date.yml");
        this.tmpFile = new File(plugin.getDataFolder(), "server_date.yml.tmp");
        this.bakFile = new File(plugin.getDataFolder(), "server_date.yml.bak");
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    @Override
    public void load() {
        File source = resolveSourceFile();

        if (source == null) {
            // Fresh start
            currentDate = plugin.getTimelineConfig().getStartDate();
            plugin.getLogger().info("[Storage] server_date.yml bulunamadı. Başlangıç tarihi kullanılıyor: " + currentDate);
            save();
            return;
        }

        if (source == bakFile) {
            plugin.getLogger().warning("[Storage] server_date.yml eksik veya bozuk. "
                    + "Yedek dosyadan (server_date.yml.bak) kurtarılıyor...");
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(source);
        parseState(cfg);

        // Copy backup source back to main file if we loaded from .bak
        if (source == bakFile) {
            try {
                Files.copy(bakFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("[Storage] Yedekten kurtarma başarılı.");
            } catch (IOException e) {
                plugin.getLogger().warning("[Storage] Yedek kopyalanamadı: " + e.getMessage());
            }
        }

        // ── Year bounds validation ─────────────────────────────────────────────
        int year    = currentDate.getYear();
        int minYear = plugin.getTimelineConfig().getMinYear();
        int maxYear = plugin.getTimelineConfig().getMaxYear();

        if (year < minYear || year > maxYear) {
            plugin.getLogger().severe("[Storage] Yüklenen tarih " + currentDate
                    + " yıl aralığı dışında [" + minYear + "-" + maxYear + "]. "
                    + "Başlangıç tarihine sıfırlanıyor: " + plugin.getTimelineConfig().getStartDate());
            currentDate = plugin.getTimelineConfig().getStartDate();
            save();
        }

        plugin.getLogger().info("[Storage] Durum yüklendi — tarih: " + currentDate
                + " | tetiklenen: " + firedEvents.size()
                + " | bayraklar: " + flagStore.getAll().size()
                + " | duraklatıldı: " + paused);
    }

    private void parseState(FileConfiguration cfg) {
        // Date
        TimelineDate startDate = plugin.getTimelineConfig().getStartDate();
        int year  = cfg.getInt("current_date.year",  startDate.getYear());
        int month = cfg.getInt("current_date.month", startDate.getMonth());
        int day   = cfg.getInt("current_date.day",   startDate.getDay());
        try {
            currentDate = new TimelineDate(year, month, day);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Storage] server_date.yml içindeki tarih bozuk ("
                    + e.getMessage() + "). Başlangıç tarihine sıfırlanıyor.");
            currentDate = startDate;
        }

        // Fired events
        firedEvents.clear();
        firedEvents.addAll(cfg.getStringList("fired_events"));

        // Flags
        flagStore.clear();
        ConfigurationSection flagSection = cfg.getConfigurationSection("flags");
        if (flagSection != null) {
            for (String key : flagSection.getKeys(false)) {
                flagStore.set(key, flagSection.getString(key, ""));
            }
        }

        // Paused
        paused = cfg.getBoolean("paused", false);
    }

    /**
     * Returns the file to load from.
     * Priority: server_date.yml → server_date.yml.bak → null (fresh start)
     */
    private File resolveSourceFile() {
        if (file.exists() && file.length() > 0) return file;
        if (bakFile.exists() && bakFile.length() > 0) return bakFile;
        return null;
    }

    // ── Save (atomic) ─────────────────────────────────────────────────────────

    @Override
    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("current_date.year",  currentDate.getYear());
        cfg.set("current_date.month", currentDate.getMonth());
        cfg.set("current_date.day",   currentDate.getDay());
        cfg.set("fired_events",       new ArrayList<>(firedEvents));
        flagStore.getAll().forEach((k, v) -> cfg.set("flags." + k, v));
        cfg.set("paused", paused);

        try {
            // 1. Write to .tmp
            cfg.save(tmpFile);

            // 2. Backup current .yml → .bak (overwrite silently)
            if (file.exists()) {
                Files.copy(file.toPath(), bakFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 3. Atomically replace .yml with .tmp
            Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[Storage] server_date.yml kaydedilemedi! Veri kaybı riski var.", e);
            tmpFile.delete(); // clean up orphaned tmp
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @Override public TimelineDate getCurrentDate()          { return currentDate; }
    @Override public void setCurrentDate(TimelineDate date) { this.currentDate = date; }

    @Override public Set<String> getFiredEvents()           { return Collections.unmodifiableSet(firedEvents); }
    @Override public void addFiredEvent(String eventId)     { firedEvents.add(eventId); }

    @Override public FlagStore getFlagStore()               { return flagStore; }

    @Override public boolean isPaused()                     { return paused; }
    @Override public void setPaused(boolean paused)         { this.paused = paused; }
}
