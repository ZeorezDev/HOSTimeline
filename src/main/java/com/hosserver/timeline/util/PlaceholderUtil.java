package com.hosserver.timeline.util;

import com.hosserver.timeline.api.TimelineDate;

/**
 * Replaces timeline placeholders in raw (pre-colorize) strings.
 *
 * Supported placeholders:
 *   {date}     — formatted date, e.g. "1 Eylül 1939"
 *   {year}     — numeric year, e.g. "1939"
 *   {month}    — numeric month, e.g. "9"
 *   {day}      — numeric day, e.g. "1"
 *   {event_id} — event identifier string
 *   {message}  — reserved for broadcast_format; NOT replaced by this utility
 */
public final class PlaceholderUtil {

    private PlaceholderUtil() {}

    /**
     * Apply all date and event placeholders to {@code template}.
     * {@code eventId} may be null — {event_id} will render as an empty string.
     */
    public static String apply(String template, TimelineDate date, String eventId) {
        if (template == null) return "";
        return template
                .replace("{date}",     date.format())
                .replace("{year}",     String.valueOf(date.getYear()))
                .replace("{month}",    String.valueOf(date.getMonth()))
                .replace("{day}",      String.valueOf(date.getDay()))
                .replace("{event_id}", eventId != null ? eventId : "");
    }

    /** Overload without eventId. */
    public static String apply(String template, TimelineDate date) {
        return apply(template, date, null);
    }
}
