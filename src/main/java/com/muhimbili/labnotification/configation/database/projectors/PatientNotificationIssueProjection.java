package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

public interface PatientNotificationIssueProjection {
    Long getPatientId();
    String getMrNumber();
    String getPatientName();
    String getPhoneNumber();
    long getFailedAttempts();
    long getMissingPhoneCount();
    Instant getLastFailure();
}
