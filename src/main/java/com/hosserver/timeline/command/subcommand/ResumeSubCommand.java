package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class ResumeSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public ResumeSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = plugin.getTimelineConfig().getPrefix();

        if (!plugin.getEngine().isPaused()) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&eOtomatik ilerleme zaten aktif."));
            return;
        }

        plugin.getEngine().resume();

        if (plugin.getTimelineConfig().isAutoAdvanceEnabled()) {
            plugin.getAutoAdvancer().start();
            sender.sendMessage(MessageUtil.colorize(prefix + "&aOtomatik ilerleme devam ettirildi."));
        } else {
            sender.sendMessage(MessageUtil.colorize(prefix + "&aDuraklatma kaldırıldı. "
                    + "&7(config.yml'de auto_advance.enabled: false — zamanlayıcı başlatılmadı)"));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.admin"; }
    @Override public String getUsage()      { return "/hostime resume"; }
}
