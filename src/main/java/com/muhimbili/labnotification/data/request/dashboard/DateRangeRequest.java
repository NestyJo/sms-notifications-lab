package com.muhimbili.labnotification.data.request.dashboard;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DateRangeRequest(
        @NotNull(message = "fromDate is required") LocalDate fromDate,
        @NotNull(message = "toDate is required") LocalDate toDate
) {
    public void validate() {
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be greater than or equal to fromDate");
        }
    }
}
