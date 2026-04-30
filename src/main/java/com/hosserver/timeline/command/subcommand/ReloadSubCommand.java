package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class ReloadSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public ReloadSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = plugin.getTimelineConfig().getPrefix();
        plugin.fullReload();
        sender.sendMessage(MessageUtil.colorize(prefix + "&aYapılandırma ve event listesi yeniden yüklendi."));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.admin"; }
    @Override public String getUsage()      { return "/hostime reload"; }
}
