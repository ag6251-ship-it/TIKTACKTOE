package com.healthsense.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private static final DateTimeFormatter DATE_TIME_SQLITE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_HM = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatDate(LocalDate date) {
        return date == null ? null : DATE.format(date);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) return null;
        return LocalDate.parse(value, DATE);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_SQLITE.format(dateTime);
    }

    public static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, DATE_TIME_SQLITE);
        } catch (DateTimeParseException ignored) {
            // Fallback to ISO if data was inserted differently
            return LocalDateTime.parse(value);
        }
    }

    public static String formatTime(LocalTime time) {
        return time == null ? null : TIME_HM.format(time);
    }

    public static LocalTime parseTime(String value) {
        if (value == null || value.isEmpty()) return null;
        return LocalTime.parse(value, TIME_HM);
    }
}


