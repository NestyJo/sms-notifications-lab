package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

/**
 * Lightweight view over orders used for SMS processing.
 */
public interface SmsOrderProjection {
    Long getId();
    String getOrderNumber();
    String getResultStatus();
    Instant getUpdatedAt();
    Integer getStatusCode();
    OrderPatientProjection getPatient();

    interface OrderPatientProjection {
        Long getId();
        String getPatientName();
        String getPhoneNumber();
    }
}
