package com.hosserver.timeline;

import com.hosserver.timeline.api.TimelineAPI;
import com.hosserver.timeline.api.integration.IntegrationRegistry;
import com.hosserver.timeline.command.TimelineCommand;
import com.hosserver.timeline.config.EventLoader;
import com.hosserver.timeline.config.TimelineConfig;
import com.hosserver.timeline.engine.AutoAdvancer;
import com.hosserver.timeline.engine.EventLog;
import com.hosserver.timeline.engine.EventScheduler;
import com.hosserver.timeline.engine.TimelineEngine;
import com.hosserver.timeline.handler.EventHandlerRegistry;
import com.hosserver.timeline.storage.StorageManager;
import com.hosserver.timeline.storage.YamlStorageImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HosTimeline extends JavaPlugin {

    private static HosTimeline instance;

    private TimelineConfig       timelineConfig;
    private StorageManager       storageManager;
    private EventLoader          eventLoader;
    private EventHandlerRegistry handlerRegistry;
    private TimelineEngine       engine;
    private EventScheduler       eventScheduler;
    private AutoAdvancer         autoAdvancer;
    private EventLog             eventLog;
    private IntegrationRegistry  integrationRegistry;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("timeline_events.yml", false);

        // Initialise in dependency order
        this.timelineConfig       = new TimelineConfig(this);
        this.storageManager       = new YamlStorageImpl(this);
        this.eventLoader          = new EventLoader(this);
        this.handlerRegistry      = new EventHandlerRegistry(this);
        this.engine               = new TimelineEngine(this);
        this.eventScheduler       = new EventScheduler(this);
        this.eventLog             = new EventLog(this);
        this.integrationRegistry  = new IntegrationRegistry(this);

        storageManager.load();
        eventLoader.load();
        engine.loadState();
        eventLog.load();

        getServer().getPluginManager().registerEvents(eventScheduler, this);

        TimelineCommand cmd = new TimelineCommand(this);
        Objects.requireNonNull(getCommand("hostime"), "Command 'hostime' missing from plugin.yml")
               .setExecutor(cmd);
        Objects.requireNonNull(getCommand("hostime"))
               .setTabCompleter(cmd);

        // Expose public API — integrationRegistry must be set before this call
        TimelineAPI.init(this);

        this.autoAdvancer = new AutoAdvancer(this);
        if (timelineConfig.isAutoAdvanceEnabled() && !engine.isPaused()) {
            autoAdvancer.start();
        }

        getLogger().info("HOS Timeline enabled. Date: " + engine.getCurrentDate().format()
                + "  |  Log: " + eventLog.getTotalEntries() + " entries");
    }

    @Override
    public void onDisable() {
        if (autoAdvancer  != null) autoAdvancer.stop();
        if (storageManager != null) storageManager.save();
        getLogger().info("HOS Timeline disabled. State saved.");
    }

    /**
     * Hot-reload: config.yml and timeline_events.yml are re-parsed.
     * Persisted state (server_date.yml, event_log.yml) is NOT touched.
     * Registered integrations are preserved.
     */
    public void fullReload() {
        if (autoAdvancer != null) autoAdvancer.stop();

        reloadConfig();
        this.timelineConfig = new TimelineConfig(this);
        eventLoader.load();

        if (timelineConfig.isAutoAdvanceEnabled() && !engine.isPaused()) {
            autoAdvancer.start();
        }

        getLogger().info("HOS Timeline reloaded. Integrations: "
                + integrationRegistry.getAll().size());
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public static HosTimeline getInstance()          { return instance; }
    public TimelineConfig       getTimelineConfig()  { return timelineConfig; }
    public StorageManager       getStorageManager()  { return storageManager; }
    public EventLoader          getEventLoader()     { return eventLoader; }
    public EventHandlerRegistry getHandlerRegistry() { return handlerRegistry; }
    public TimelineEngine       getEngine()          { return engine; }
    public AutoAdvancer         getAutoAdvancer()    { return autoAdvancer; }
    public EventLog             getEventLog()        { return eventLog; }
    public IntegrationRegistry  getIntegrationRegistry() { return integrationRegistry; }
}
