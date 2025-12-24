package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public interface BatchProjector {
    Long getId();
    LocalDate getResultsDate();
    LocalTime getFromTime();
    LocalTime getToTime();
    String getStatus();
    Instant getFetchedAt();
    String getPayloadHash();
}
