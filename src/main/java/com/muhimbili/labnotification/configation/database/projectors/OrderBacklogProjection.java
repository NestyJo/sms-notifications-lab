package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;
import java.time.LocalDateTime;

public interface OrderBacklogProjection {
    Long getId();
    String getOrderNumber();
    String getOrderStatus();
    String getResultStatus();
    String getOrderType();
    String getPatientMrNumber();
    String getPatientName();
    LocalDateTime getCollectedAt();
    Instant getCreatedAt();
}
