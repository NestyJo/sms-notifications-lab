package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SmsErrorBreakdownResponse {
    @Singular
    List<ErrorItem> errors;

    @Value
    @Builder
    public static class ErrorItem {
        Integer statusCode;
        String errorMessage;
        long occurrences;
        String mostRecentOccurrence;
    }
}
