package com.hosserver.timeline.command.subcommand;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.engine.EventLog;
import com.hosserver.timeline.model.EventLogEntry;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /hostime log [sayfa]
 * Shows the event fire history in reverse chronological order.
 */
public final class LogSubCommand implements SubCommand {

    private final HosTimeline plugin;

    public LogSubCommand(HosTimeline plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String prefix = plugin.getTimelineConfig().getPrefix();
        EventLog log  = plugin.getEventLog();

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtil.colorize(prefix + "&cGeçersiz sayfa numarası."));
                return;
            }
        }

        int total      = log.getTotalEntries();
        int totalPages = log.getTotalPages(EventLog.PAGE_SIZE);

        if (total == 0) {
            sender.sendMessage(MessageUtil.colorize(prefix + "&7Henüz hiç event tetiklenmedi."));
            return;
        }

        List<EventLogEntry> entries = log.getPage(page, EventLog.PAGE_SIZE);
        if (entries.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize(prefix
                    + "&cSayfa " + page + " bulunamadı. Toplam " + totalPages + " sayfa var."));
            return;
        }

        sender.sendMessage(MessageUtil.colorize(
                prefix + "&6Event Geçmişi &8(&e" + total + " kayıt&8) &7— Sayfa &e"
                + page + "&7/&e" + totalPages));

        for (EventLogEntry e : entries) {
            sender.sendMessage(MessageUtil.colorize(
                    "&8  " + e.getRealTimestamp()
                    + "  &7|  &e" + e.getGameDate().format()
                    + "  &7|  &f" + e.getEventId()
                    + "  &8[&7" + e.getEventType().name() + "&8]"));
        }

        if (page < totalPages) {
            sender.sendMessage(MessageUtil.colorize(
                    "&7Sonraki sayfa: &e/hostime log " + (page + 1)));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            int totalPages = plugin.getEventLog().getTotalPages(EventLog.PAGE_SIZE);
            if (totalPages >= 1) {
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= Math.min(totalPages, 9); i++) {
                    pages.add(String.valueOf(i));
                }
                return pages;
            }
        }
        return Collections.emptyList();
    }

    @Override public String getPermission() { return "hostime.admin"; }
    @Override public String getUsage()      { return "/hostime log [sayfa]"; }
}
