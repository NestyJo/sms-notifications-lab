package com.muhimbili.labnotification.configation.database.projectors;

import com.muhimbili.labnotification.configation.database.entities.Order;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface OrderProjector {
    Long getId();
    String getOrderNumber();
    LocalDate getOrderDate();
    LocalTime getOrderTime();
    LocalDateTime getCollectedAt();
    String getOrderStatus();
    String getResultStatus();
    String getOrderType();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    Order.ProcessingStatus getStatus();
    Integer getStatusCode();
}
