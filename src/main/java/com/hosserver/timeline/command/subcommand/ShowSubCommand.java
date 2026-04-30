package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class ShowSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public ShowSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        TimelineDate date = plugin.getEngine().getCurrentDate();
        boolean paused    = plugin.getEngine().isPaused();
        boolean autoOn    = plugin.getTimelineConfig().isAutoAdvanceEnabled();
        String prefix     = plugin.getTimelineConfig().getPrefix();

        sender.sendMessage(MessageUtil.colorize(prefix + "&eSunucu Tarihi: &f&l" + date.format()));
        sender.sendMessage(MessageUtil.colorize(prefix + "&eOtomatik ilerleme: "
                + (autoOn ? (paused ? "&cDuraklatıldı" : "&aAktif") : "&7Devre dışı")));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.use"; }
    @Override public String getUsage()      { return "/hostime show"; }
}
