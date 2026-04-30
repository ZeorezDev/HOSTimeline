package com.hosserver.timeline.handler;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.model.TimelineEvent;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Dispatches one or more commands as the console.
 *
 * Required payload key: commands (List<String>)
 *
 * Commands must NOT include the leading '/'.
 * They run on the main thread in the order listed.
 */
public final class ConsoleCommandHandler implements EventHandler {

    private final HosTimeline plugin;

    public ConsoleCommandHandler(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(TimelineEvent event) {
        Object raw = event.getPayload().get("commands");
        if (raw == null) {
            plugin.getLogger().warning("CONSOLE_COMMAND event '" + event.getId() + "' has no 'commands' in payload. Skipping.");
            return;
        }

        List<String> commands;
        try {
            commands = (List<String>) raw;
        } catch (ClassCastException e) {
            plugin.getLogger().warning("CONSOLE_COMMAND event '" + event.getId() + "': 'commands' must be a YAML string list. Skipping.");
            return;
        }

        if (commands.isEmpty()) {
            plugin.getLogger().warning("CONSOLE_COMMAND event '" + event.getId() + "' has an empty commands list. Skipping.");
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String cmd : commands) {
                if (cmd == null || cmd.isBlank()) continue;
                // Strip leading slash if present — dispatchCommand does not expect it
                String clean = cmd.startsWith("/") ? cmd.substring(1) : cmd;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clean);
            }
        });
    }
}
