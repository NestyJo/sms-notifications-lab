package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class DashboardOverviewResponse {
    LocalDate fromDate;
    LocalDate toDate;
    long totalPatients;
    long ordersCreated;
    long ordersCompleted;
    long testsCompleted;
    BigDecimal smsSuccessRate;
}
