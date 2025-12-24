package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

public interface TestResultProjector {
    Long getId();
    String getTestCode();
    String getTestName();
    String getResultStatus();
    String getOrderStatus();
    String getOrderType();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    String getTatTime();
}
