package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class OrderTrendPoint {
    LocalDate bucketStart;
    LocalDate bucketEnd;
    long ordersCreated;
    long ordersCompleted;
}
