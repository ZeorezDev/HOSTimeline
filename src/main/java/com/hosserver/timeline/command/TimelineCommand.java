package com.hosserver.timeline.command;

import com.hosserver.timeline.HosTimeline;
import com.hosserver.timeline.command.subcommand.*;
import com.hosserver.timeline.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public final class TimelineCommand implements CommandExecutor, TabCompleter {

    private final HosTimeline plugin;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public TimelineCommand(HosTimeline plugin) {
        this.plugin = plugin;
        register("show",    new ShowSubCommand(plugin));
        register("set",     new SetSubCommand(plugin));
        register("advance", new AdvanceSubCommand(plugin));
        register("pause",   new PauseSubCommand(plugin));
        register("resume",  new ResumeSubCommand(plugin));
        register("reload",  new ReloadSubCommand(plugin));
        register("log",     new LogSubCommand(plugin));
    }

    private void register(String name, SubCommand cmd) {
        subCommands.put(name.toLowerCase(), cmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage(MessageUtil.colorize(
                    plugin.getTimelineConfig().getPrefix() + "&cBilinmeyen alt komut: &e" + args[0]));
            sendHelp(sender, label);
            return true;
        }

        if (!sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(MessageUtil.colorize(
                    plugin.getTimelineConfig().getPrefix() + "&cBu komutu kullanma izniniz yok."));
            return true;
        }

        sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                if (entry.getKey().startsWith(args[0].toLowerCase())
                        && sender.hasPermission(entry.getValue().getPermission())) {
                    completions.add(entry.getKey());
                }
            }
            return completions;
        }

        if (args.length > 1) {
            SubCommand sub = subCommands.get(args[0].toLowerCase());
            if (sub != null && sender.hasPermission(sub.getPermission())) {
                return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return Collections.emptyList();
    }

    private void sendHelp(CommandSender sender, String label) {
        String p = plugin.getTimelineConfig().getPrefix();
        sender.sendMessage(MessageUtil.colorize(p + "&6HOS Timeline Komutları:"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " show              &7— Mevcut tarihi göster"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " set <yıl> <ay> <gün> &7— Tarihi ayarla"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " advance <gün>     &7— Tarihi ilerlet"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " pause             &7— Otomatik ilerlemeyi duraklat"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " resume            &7— Otomatik ilerlemeyi devam ettir"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " log [sayfa]       &7— Event geçmişini göster"));
        sender.sendMessage(MessageUtil.colorize("  &e/" + label + " reload            &7— Config ve eventleri yeniden yükle"));
    }
}
