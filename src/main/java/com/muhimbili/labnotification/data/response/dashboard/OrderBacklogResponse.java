package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OrderBacklogResponse {
    long totalPending;
    long initialCount;
    long processingCount;
    @Singular
    List<OrderSummary> oldestOrders;

    @Value
    @Builder
    public static class OrderSummary {
        Long orderId;
        String orderNumber;
        String patientMrn;
        String patientName;
        String orderStatus;
        String resultStatus;
        String orderType;
        String collectedAt;
        String createdAt;
    }
}
