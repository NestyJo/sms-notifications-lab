package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

public interface  LabDepartmentProjector {
    Long getId();
    String getCode();
    String getDescription();
    String getLabName();
    String getLabType();
    String getLabCode();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
