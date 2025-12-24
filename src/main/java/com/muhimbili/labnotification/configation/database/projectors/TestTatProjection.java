package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TestTatProjection {
    Long getTestId();
    Long getDepartmentId();
    String getDepartmentName();
    String getTestCode();
    String getTestName();
    LocalDate getOrderDate();
    LocalDateTime getCollectedAt();
    Instant getResultUpdatedAt();
}
