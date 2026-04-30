package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class AdvanceSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public AdvanceSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = plugin.getTimelineConfig().getPrefix();

        if (args.length < 1) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&cKullanım: " + getUsage()));
            return;
        }

        int days;
        try {
            days = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&cGeçersiz format — tam sayı girin."));
            return;
        }

        if (days < 1) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&cİlerleme en az 1 gün olmalı."));
            return;
        }

        // ── Safety: max advance limit ─────────────────────────────────────────
        int maxAdvance = plugin.getTimelineConfig().getMaxAdvanceDays();
        if (maxAdvance > 0 && days > maxAdvance) {
            sender.sendMessage(MessageUtil.colorize(prefix
                    + "&cMaksimum tek seferde &e" + maxAdvance + " &cgün ilerlenebilir. "
                    + "(config: safety.max_advance_days)"));
            sender.sendMessage(MessageUtil.colorize(prefix
                    + "&7Daha büyük atlamalar için &e/hostime set <yıl> <ay> <gün> &7kullanın."));
            return;
        }

        // ── Safety: year bounds check ─────────────────────────────────────────
        TimelineDate projected = plugin.getEngine().getCurrentDate().plusDays(days);
        int minYear = plugin.getTimelineConfig().getMinYear();
        int maxYear = plugin.getTimelineConfig().getMaxYear();
        if (projected.getYear() < minYear || projected.getYear() > maxYear) {
            sender.sendMessage(MessageUtil.colorize(prefix
                    + "&cHedef tarih (&e" + projected.format() + "&c) izin verilen yıl aralığı dışında "
                    + "[&e" + minYear + "&c-&e" + maxYear + "&c]."));
            return;
        }

        plugin.getEngine().advance(days);
        sender.sendMessage(MessageUtil.colorize(prefix
                + "&a+" + days + " gün ilerlendi. &fYeni tarih: &l"
                + plugin.getEngine().getCurrentDate().format()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("1", "7", "30", "90", "365");
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.admin"; }
    @Override public String getUsage()      { return "/hostime advance <gün>"; }
}
