package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PatientNotificationIssueResponse {
    @Singular
    List<Issue> issues;

    @Value
    @Builder
    public static class Issue {
        Long patientId;
        String mrNumber;
        String patientName;
        String phoneNumber;
        long failedAttempts;
        long missingPhoneCount;
        String lastFailure;
    }
}
