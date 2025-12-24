package com.muhimbili.labnotification.rest;

import com.muhimbili.labnotification.data.response.dashboard.DashboardOverviewResponse;
import com.muhimbili.labnotification.data.response.dashboard.OrderBacklogResponse;
import com.muhimbili.labnotification.data.response.dashboard.OrderTrendPoint;
import com.muhimbili.labnotification.data.response.dashboard.PatientNotificationIssueResponse;
import com.muhimbili.labnotification.data.response.dashboard.SmsErrorBreakdownResponse;
import com.muhimbili.labnotification.data.response.dashboard.SmsSummaryResponse;
import com.muhimbili.labnotification.data.response.dashboard.TestDepartmentStatsResponse;
import com.muhimbili.labnotification.data.response.dashboard.TestTatStatsResponse;
import com.muhimbili.labnotification.service.dashboard.DashboardMetricsService;
import com.muhimbili.labnotification.utility.DateRangeUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Validated
public class DashboardController {

    private static final int DEFAULT_TREND_LIMIT_DAYS = 14;
    private static final int DEFAULT_BACKLOG_LIMIT = 10;
    private static final int DEFAULT_SMS_ERROR_LIMIT = 5;
    private static final long DEFAULT_PATIENT_ISSUE_THRESHOLD = 3L;

    private final DashboardMetricsService dashboardMetricsService;

    public DashboardController(DashboardMetricsService dashboardMetricsService) {
        this.dashboardMetricsService = dashboardMetricsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> overview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        DashboardOverviewResponse response = dashboardMetricsService.getOverview(normalizedFrom, normalizedTo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/trend")
    public ResponseEntity<List<OrderTrendPoint>> orderTrend(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "DAY") DashboardMetricsService.TimeSeriesGranularity granularity) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        // Restrict the window to avoid unbounded queries.
        if (normalizedFrom.isBefore(normalizedTo.minusDays(90))) {
            normalizedFrom = normalizedTo.minusDays(DEFAULT_TREND_LIMIT_DAYS);
        }

        return ResponseEntity.ok(
                dashboardMetricsService.getOrderTrend(normalizedFrom, normalizedTo, granularity)
        );
    }

    @GetMapping("/orders/backlog")
    public ResponseEntity<OrderBacklogResponse> orderBacklog(
            @RequestParam(defaultValue = "" + DEFAULT_BACKLOG_LIMIT)
            @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(dashboardMetricsService.getOrderBacklog(limit));
    }

    @GetMapping("/tests/by-department")
    public ResponseEntity<TestDepartmentStatsResponse> testsByDepartment(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long departmentId) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ResponseEntity.ok(
                dashboardMetricsService.getDepartmentStats(normalizedFrom, normalizedTo, departmentId)
        );
    }

    @GetMapping("/tests/tat")
    public ResponseEntity<TestTatStatsResponse> testTat(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long departmentId) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ResponseEntity.ok(
                dashboardMetricsService.getTatStats(normalizedFrom, normalizedTo, departmentId)
        );
    }

    @GetMapping("/sms/summary")
    public ResponseEntity<SmsSummaryResponse> smsSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ResponseEntity.ok(
                dashboardMetricsService.getSmsSummary(normalizedFrom, normalizedTo)
        );
    }

    @GetMapping("/sms/errors")
    public ResponseEntity<SmsErrorBreakdownResponse> smsErrors(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "" + DEFAULT_SMS_ERROR_LIMIT)
            @Min(1) @Max(50) int limit) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ResponseEntity.ok(
                dashboardMetricsService.getSmsErrorBreakdown(normalizedFrom, normalizedTo, limit)
        );
    }

    @GetMapping("/sms/patient-issues")
    public ResponseEntity<PatientNotificationIssueResponse> patientIssues(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "" + DEFAULT_PATIENT_ISSUE_THRESHOLD)
            @PositiveOrZero long threshold) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ResponseEntity.ok(
                dashboardMetricsService.getPatientNotificationIssues(normalizedFrom, normalizedTo, threshold)
        );
    }
}
