package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;
import java.time.LocalTime;

public interface LabResultWindowProjector {
    Long getId();
    String getResultsDate();
    LocalTime getFromTime();
    LocalTime getToTime();
    Integer getIntervalMinutes();
    Instant getLastRunAt();
    Boolean getEnabled();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
