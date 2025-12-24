package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

public interface SmsErrorAggregation {
    Integer getStatusCode();
    String getErrorMessage();
    long getOccurrences();
    Instant getMostRecentOccurrence();
}
