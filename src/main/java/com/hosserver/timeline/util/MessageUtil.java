package com.hosserver.timeline.util;

import org.bukkit.ChatColor;

public final class MessageUtil {

    private MessageUtil() {}

    /** Translates '&' colour codes to Minecraft formatting characters. */
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
