package com.muhimbili.labnotification.service.dashboard;

import com.muhimbili.labnotification.data.response.dashboard.DashboardOverviewResponse;
import com.muhimbili.labnotification.data.response.dashboard.OrderBacklogResponse;
import com.muhimbili.labnotification.data.response.dashboard.OrderTrendPoint;
import com.muhimbili.labnotification.data.response.dashboard.PatientNotificationIssueResponse;
import com.muhimbili.labnotification.data.response.dashboard.SmsErrorBreakdownResponse;
import com.muhimbili.labnotification.data.response.dashboard.SmsSummaryResponse;
import com.muhimbili.labnotification.data.response.dashboard.TestDepartmentStatsResponse;
import com.muhimbili.labnotification.data.response.dashboard.TestTatStatsResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardMetricsService {

    DashboardOverviewResponse getOverview(LocalDate fromDate, LocalDate toDate);

    List<OrderTrendPoint> getOrderTrend(LocalDate fromDate, LocalDate toDate, TimeSeriesGranularity granularity);

    OrderBacklogResponse getOrderBacklog(int limit);

    TestDepartmentStatsResponse getDepartmentStats(LocalDate fromDate, LocalDate toDate, Long departmentId);

    TestTatStatsResponse getTatStats(LocalDate fromDate, LocalDate toDate, Long departmentId);

    SmsSummaryResponse getSmsSummary(LocalDate fromDate, LocalDate toDate);

    SmsErrorBreakdownResponse getSmsErrorBreakdown(LocalDate fromDate, LocalDate toDate, int limit);

    PatientNotificationIssueResponse getPatientNotificationIssues(LocalDate fromDate, LocalDate toDate, long threshold);

    enum TimeSeriesGranularity {
        DAY,
        WEEK
    }
}
