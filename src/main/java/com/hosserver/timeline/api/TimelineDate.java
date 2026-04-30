package com.hosserver.timeline.api;

import java.util.Objects;

/**
 * Immutable value object representing a date in the server timeline.
 * Supports arithmetic and comparison. No time-of-day concept.
 */
public final class TimelineDate implements Comparable<TimelineDate> {

    private static final int[] DAYS_IN_MONTH = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final String[] MONTH_NAMES_TR = {
        "", "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    };

    private final int year;
    private final int month;
    private final int day;

    public TimelineDate(int year, int month, int day) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Ay 1-12 arasında olmalı, alınan: " + month);
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Gün 1-31 arasında olmalı, alınan: " + day);
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear()  { return year; }
    public int getMonth() { return month; }
    public int getDay()   { return day; }

    /**
     * Numeric key for fast comparison and range queries: YYYYMMDD.
     */
    public long toLong() {
        return (long) year * 10_000L + month * 100L + day;
    }

    /**
     * Returns a new TimelineDate offset by the given number of days.
     * Negative values move backward. Does not mutate this instance.
     */
    public TimelineDate plusDays(int days) {
        int d = day + days;
        int m = month;
        int y = year;

        // Roll forward
        while (d > daysInMonth(m, y)) {
            d -= daysInMonth(m, y);
            m++;
            if (m > 12) { m = 1; y++; }
        }
        // Roll backward
        while (d < 1) {
            m--;
            if (m < 1) { m = 12; y--; }
            d += daysInMonth(m, y);
        }
        return new TimelineDate(y, m, d);
    }

    /** Human-readable Turkish format, e.g. "1 Eylül 1939". */
    public String format() {
        return day + " " + MONTH_NAMES_TR[month] + " " + year;
    }

    /** Machine-readable ISO format: YYYY-MM-DD. */
    @Override
    public String toString() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    @Override
    public int compareTo(TimelineDate other) {
        return Long.compare(this.toLong(), other.toLong());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimelineDate that)) return false;
        return year == that.year && month == that.month && day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static int daysInMonth(int month, int year) {
        if (month == 2 && isLeapYear(year)) return 29;
        return DAYS_IN_MONTH[month];
    }

    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
