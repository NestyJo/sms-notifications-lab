package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Duration;
import java.util.List;

@Value
@Builder
public class TestTatStatsResponse {
    @Singular
    List<TatMetric> metrics;

    @Value
    @Builder
    public static class TatMetric {
        Long departmentId;
        String departmentName;
        String testCode;
        String testName;
        Duration averageTat;
        Duration medianTat;
        Duration percentile90Tat;
        long sampleSize;
    }
}
