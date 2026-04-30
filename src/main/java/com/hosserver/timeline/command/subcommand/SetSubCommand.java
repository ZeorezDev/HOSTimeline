package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.api.TimelineDate;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class SetSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public SetSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = plugin.getTimelineConfig().getPrefix();

        if (args.length < 3) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&cKullanım: " + getUsage()));
            return;
        }

        int year, month, day;
        try {
            year  = Integer.parseInt(args[0]);
            month = Integer.parseInt(args[1]);
            day   = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&cGeçersiz format — yıl, ay ve gün tam sayı olmalı."));
            return;
        }

        TimelineDate newDate;
        try {
            newDate = new TimelineDate(year, month, day);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&cGeçersiz tarih: " + e.getMessage()));
            return;
        }

        plugin.getEngine().setDate(newDate);
        sender.sendMessage(MessageUtil.colorize(prefix + "&aTarih ayarlandı: &f&l" + newDate.format()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("1936", "1939", "1941", "1945");
        if (args.length == 2) return List.of("1", "6", "9", "12");
        if (args.length == 3) return List.of("1", "15", "30");
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.admin"; }
    @Override public String getUsage()      { return "/hostime set <yıl> <ay> <gün>"; }
}
