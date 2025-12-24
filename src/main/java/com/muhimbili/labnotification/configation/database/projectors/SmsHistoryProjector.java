package com.muhimbili.labnotification.configation.database.projectors;

import java.time.Instant;
import java.time.LocalDateTime;

public interface SmsHistoryProjector {
    Long getId();
    Long getNotificationId();
    String getPhoneNumber();
    String getMessageBody();
    String getProviderMessageId();
    String getStatus();
    String getErrorMessage();
    LocalDateTime getSentAt();
    LocalDateTime getDeliveryAt();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}
