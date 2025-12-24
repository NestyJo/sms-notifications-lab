package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class SmsSummaryResponse {
    LocalDate fromDate;
    LocalDate toDate;
    long totalSent;
    long totalDelivered;
    long totalFailed;
    BigDecimal deliveryRate;
    BigDecimal averageDeliveryLatencySeconds;
}
