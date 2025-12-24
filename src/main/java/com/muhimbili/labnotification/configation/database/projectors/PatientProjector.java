package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

public interface PatientProjector {
    Long getId();
    String getMrNumber();
    String getPatientName();
    String getPhoneNumber();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
