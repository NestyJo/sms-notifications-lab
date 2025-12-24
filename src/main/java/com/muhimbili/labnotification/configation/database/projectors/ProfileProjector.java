package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;

public interface ProfileProjector {
    Long getId();
    String getProfileCode();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
