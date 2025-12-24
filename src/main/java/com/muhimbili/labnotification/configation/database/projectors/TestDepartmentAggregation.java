package com.muhimbili.labnotification.configation.database.projectors;

public interface TestDepartmentAggregation {
    Long getDepartmentId();
    String getDepartmentName();
    long getTotalTests();
    long getPendingResults();
    long getFinalizedResults();
    long getAbnormalResults();
}
