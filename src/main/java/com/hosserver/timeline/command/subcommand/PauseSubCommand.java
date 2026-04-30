package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class PauseSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public PauseSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = plugin.getTimelineConfig().getPrefix();

        if (plugin.getEngine().isPaused()) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&eOtomatik ilerleme zaten duraklatılmış."));
            return;
        }

        plugin.getEngine().pause();
        plugin.getAutoAdvancer().stop();

        sender.sendMessage(MessageUtil.colorize(prefix + "&cOtomatik ilerleme duraklatıldı."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.admin"; }
    @Override public String getUsage()      { return "/hostime pause"; }
}
