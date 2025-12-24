package com.muhimbili.labnotification.utility;

import java.time.LocalDate;

public final class DateRangeUtils {

    private DateRangeUtils() {
    }

    public static LocalDate defaultFrom(LocalDate fromDate) {
        return fromDate != null ? fromDate : LocalDate.now().minusDays(6);
    }

    public static LocalDate defaultTo(LocalDate toDate) {
        return toDate != null ? toDate : LocalDate.now();
    }

    public static void validateChronology(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be on or before toDate");
        }
    }
}
