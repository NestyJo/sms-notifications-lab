package com.muhimbili.labnotification.data.request.dashboard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SmsIssueFilterRequest(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @Min(value = 1, message = "threshold must be at least 1") long threshold
) {
    public void validate() {
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must be after fromDate");
        }
    }
}
