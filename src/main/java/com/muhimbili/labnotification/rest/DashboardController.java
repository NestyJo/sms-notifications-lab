package com.muhimbili.labnotification.rest;

import com.muhimbili.labnotification.data.response.ApiResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
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
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> overview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        DashboardOverviewResponse response = dashboardMetricsService.getOverview(normalizedFrom, normalizedTo);
        return ok(response, "Dashboard overview fetched successfully");
    }

    @GetMapping("/orders/trend")
    public ResponseEntity<ApiResponse<List<OrderTrendPoint>>> orderTrend(
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

        return ok(
                dashboardMetricsService.getOrderTrend(normalizedFrom, normalizedTo, granularity),
                "Order trend generated successfully"
        );
    }

    @GetMapping("/orders/backlog")
    public ResponseEntity<ApiResponse<OrderBacklogResponse>> orderBacklog(
            @RequestParam(defaultValue = "" + DEFAULT_BACKLOG_LIMIT)
            @Min(1) @Max(100) int limit) {
        return ok(dashboardMetricsService.getOrderBacklog(limit), "Order backlog retrieved successfully");
    }

    @GetMapping("/tests/by-department")
    public ResponseEntity<ApiResponse<TestDepartmentStatsResponse>> testsByDepartment(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long departmentId) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ok(
                dashboardMetricsService.getDepartmentStats(normalizedFrom, normalizedTo, departmentId),
                "Department statistics calculated successfully"
        );
    }

    @GetMapping("/tests/tat")
    public ResponseEntity<ApiResponse<TestTatStatsResponse>> testTat(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long departmentId) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ok(
                dashboardMetricsService.getTatStats(normalizedFrom, normalizedTo, departmentId),
                "Test turnaround time stats fetched successfully"
        );
    }

    @GetMapping("/sms/summary")
    public ResponseEntity<ApiResponse<SmsSummaryResponse>> smsSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ok(
                dashboardMetricsService.getSmsSummary(normalizedFrom, normalizedTo),
                "SMS summary generated successfully"
        );
    }

    @GetMapping("/sms/errors")
    public ResponseEntity<ApiResponse<SmsErrorBreakdownResponse>> smsErrors(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "" + DEFAULT_SMS_ERROR_LIMIT)
            @Min(1) @Max(50) int limit) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ok(
                dashboardMetricsService.getSmsErrorBreakdown(normalizedFrom, normalizedTo, limit),
                "SMS error breakdown compiled successfully"
        );
    }

    @GetMapping("/sms/patient-issues")
    public ResponseEntity<ApiResponse<PatientNotificationIssueResponse>> patientIssues(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "" + DEFAULT_PATIENT_ISSUE_THRESHOLD)
            @PositiveOrZero long threshold) {
        LocalDate normalizedFrom = DateRangeUtils.defaultFrom(fromDate);
        LocalDate normalizedTo = DateRangeUtils.defaultTo(toDate);
        DateRangeUtils.validateChronology(normalizedFrom, normalizedTo);

        return ok(
                dashboardMetricsService.getPatientNotificationIssues(normalizedFrom, normalizedTo, threshold),
                "Patient notification issues listed successfully"
        );
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message(message)
                        .data(data)
                        .build()
        );
    }
}
