package com.hosserver.timeline.engine;

import com.hosserver.timeline.HosTimeline;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically advances the timeline by a configured number of days.
 * Respects the engine's paused flag — if paused, the task is a no-op that tick.
 *
 * Call start() to (re)activate and stop() to cancel.
 */
public final class AutoAdvancer {

    private final HosTimeline plugin;
    private BukkitTask task;

    public AutoAdvancer(HosTimeline plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop(); // cancel any existing task before creating a new one

        long interval = plugin.getTimelineConfig().getAutoAdvanceIntervalTicks();
        int  days     = plugin.getTimelineConfig().getAutoAdvanceDays();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getEngine().isPaused()) return;
                plugin.getEngine().advance(days);
                plugin.getLogger().fine("[Timeline] Auto-advanced " + days + " day(s). Now: "
                        + plugin.getEngine().getCurrentDate());
            }
        }.runTaskTimer(plugin, interval, interval);

        plugin.getLogger().info("[Timeline] Auto-advance started: every " + interval
                + " ticks, +" + days + " day(s).");
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
            plugin.getLogger().info("[Timeline] Auto-advance stopped.");
        }
    }

    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }
}
