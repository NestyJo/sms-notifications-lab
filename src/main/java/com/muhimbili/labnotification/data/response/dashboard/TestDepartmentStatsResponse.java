package com.muhimbili.labnotification.data.response.dashboard;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TestDepartmentStatsResponse {
    @Singular
    List<DepartmentStats> departments;

    @Value
    @Builder
    public static class DepartmentStats {
        Long departmentId;
        String departmentName;
        long totalTests;
        long pendingResults;
        long finalizedResults;
        long abnormalResults;
    }
}
