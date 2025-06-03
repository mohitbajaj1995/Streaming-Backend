package com.easyliveline.streamingbackend.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static int getTodayAsInt() {
        LocalDate today = LocalDate.now();
        return today.getYear() * 10000 + today.getMonthValue() * 100 + today.getDayOfMonth();
    }

    public static int getDateAfterDays(int daysToAdd) {
        return Integer.parseInt(LocalDate.now().plusDays(daysToAdd).format(FORMATTER));
    }

    public static int getDateAfterMonths(int monthsToAdd) {
        return Integer.parseInt(LocalDate.now().plusMonths(monthsToAdd).format(FORMATTER));
    }

    // ✅ Add both months and days
    public static int getDateAfterMonthsAndDays(int monthsToAdd, int daysToAdd) {
        return Integer.parseInt(LocalDate.now()
                .plusMonths(monthsToAdd)
                .plusDays(daysToAdd)
                .format(FORMATTER));
    }

    public static LocalDate fromInt(int dateInt) {
        return LocalDate.parse(String.valueOf(dateInt), FORMATTER);
    }
}