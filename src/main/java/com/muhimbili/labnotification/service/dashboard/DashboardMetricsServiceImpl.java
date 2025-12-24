package com.muhimbili.labnotification.service.dashboard;

import com.muhimbili.labnotification.configation.database.entities.Order;
import com.muhimbili.labnotification.configation.database.projectors.OrderBacklogProjection;
import com.muhimbili.labnotification.configation.database.projectors.OrderDailyAggregation;
import com.muhimbili.labnotification.configation.database.projectors.PatientNotificationIssueProjection;
import com.muhimbili.labnotification.configation.database.projectors.SmsErrorAggregation;
import com.muhimbili.labnotification.configation.database.projectors.TestDepartmentAggregation;
import com.muhimbili.labnotification.configation.database.projectors.TestTatProjection;
import com.muhimbili.labnotification.configation.database.repository.OrderRepository;
import com.muhimbili.labnotification.configation.database.repository.PatientRepository;
import com.muhimbili.labnotification.configation.database.repository.SmsHistoryRepository;
import com.muhimbili.labnotification.configation.database.repository.TestResultRepository;
import com.muhimbili.labnotification.data.response.dashboard.DashboardOverviewResponse;
import com.muhimbili.labnotification.data.response.dashboard.OrderBacklogResponse;
import com.muhimbili.labnotification.data.response.dashboard.OrderTrendPoint;
import com.muhimbili.labnotification.data.response.dashboard.PatientNotificationIssueResponse;
import com.muhimbili.labnotification.data.response.dashboard.SmsErrorBreakdownResponse;
import com.muhimbili.labnotification.data.response.dashboard.SmsSummaryResponse;
import com.muhimbili.labnotification.data.response.dashboard.TestDepartmentStatsResponse;
import com.muhimbili.labnotification.data.response.dashboard.TestTatStatsResponse;
import com.muhimbili.labnotification.service.dashboard.DashboardMetricsService.TimeSeriesGranularity;
import com.muhimbili.labnotification.utility.LoggerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardMetricsServiceImpl implements DashboardMetricsService {

    private static final List<Order.ProcessingStatus> BACKLOG_STATUSES = List.of(
            Order.ProcessingStatus.INITIAL,
            Order.ProcessingStatus.PROCESSING
    );

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private final PatientRepository patientRepository;
    private final OrderRepository orderRepository;
    private final TestResultRepository testResultRepository;
    private final SmsHistoryRepository smsHistoryRepository;
    private final LoggerService loggerService;

    public DashboardMetricsServiceImpl(PatientRepository patientRepository,
                                       OrderRepository orderRepository,
                                       TestResultRepository testResultRepository,
                                       SmsHistoryRepository smsHistoryRepository,
                                       LoggerService loggerService) {
        this.patientRepository = patientRepository;
        this.orderRepository = orderRepository;
        this.testResultRepository = testResultRepository;
        this.smsHistoryRepository = smsHistoryRepository;
        this.loggerService = loggerService;
    }

    @Override
    public DashboardOverviewResponse getOverview(LocalDate fromDate, LocalDate toDate) {
        loggerService.info("dashboard_metrics -> computing overview from {} to {}", fromDate, toDate);
        Instant from = startOfDay(fromDate);
        Instant to = endOfDayExclusive(toDate);
        LocalDateTime smsFrom = startOfDayLocal(fromDate);
        LocalDateTime smsTo = endOfDayExclusiveLocal(toDate);

        long ordersCreated = orderRepository.countOrdersCreatedBetween(from, to);
        long ordersCompleted = orderRepository.countOrdersCompletedBetween(from, to);
        long testsCompleted = testResultRepository.countCompletedBetween(from, to);
        long totalSent = smsHistoryRepository.countSentBetween(smsFrom, smsTo);
        long totalDelivered = smsHistoryRepository.countDeliveredBetween(smsFrom, smsTo);
        BigDecimal smsSuccess = computeRate(totalDelivered, totalSent);

        return DashboardOverviewResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalPatients(patientRepository.count())
                .ordersCreated(ordersCreated)
                .ordersCompleted(ordersCompleted)
                .testsCompleted(testsCompleted)
                .smsSuccessRate(smsSuccess)
                .build();
    }

    @Override
    public List<OrderTrendPoint> getOrderTrend(LocalDate fromDate, LocalDate toDate, TimeSeriesGranularity granularity) {
        TimeSeriesGranularity effectiveGranularity = granularity != null ? granularity : TimeSeriesGranularity.DAY;
        loggerService.info("dashboard_metrics -> fetching order trend from {} to {} with granularity {}",
                fromDate, toDate, effectiveGranularity);

        Map<LocalDate, Long> createdMap = orderRepository.countDailyOrders(fromDate, toDate)
                .stream()
                .collect(Collectors.toMap(OrderDailyAggregation::getOrderDate,
                        OrderDailyAggregation::getCount,
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<LocalDate, Long> completedMap = orderRepository.countDailyCompletedOrders(fromDate, toDate)
                .stream()
                .collect(Collectors.toMap(OrderDailyAggregation::getOrderDate,
                        OrderDailyAggregation::getCount,
                        (left, right) -> left,
                        LinkedHashMap::new));

        List<OrderTrendPoint> points = new ArrayList<>();
        int step = effectiveGranularity == TimeSeriesGranularity.DAY ? 1 : 7;
        LocalDate bucketStart = fromDate;
        while (!bucketStart.isAfter(toDate)) {
            LocalDate bucketEnd = bucketStart.plusDays(step - 1);
            if (bucketEnd.isAfter(toDate)) {
                bucketEnd = toDate;
            }
            long created = sumCounts(createdMap, bucketStart, bucketEnd);
            long completed = sumCounts(completedMap, bucketStart, bucketEnd);
            points.add(OrderTrendPoint.builder()
                    .bucketStart(bucketStart)
                    .bucketEnd(bucketEnd)
                    .ordersCreated(created)
                    .ordersCompleted(completed)
                    .build());
            bucketStart = bucketEnd.plusDays(1);
        }
        return points;
    }

    @Override
    public OrderBacklogResponse getOrderBacklog(int limit) {
        loggerService.info("dashboard_metrics -> computing order backlog with limit {}", limit);
        long totalPending = orderRepository.countByStatuses(BACKLOG_STATUSES);
        long initialCount = orderRepository.countByStatuses(List.of(Order.ProcessingStatus.INITIAL));
        long processingCount = orderRepository.countByStatuses(List.of(Order.ProcessingStatus.PROCESSING));

        List<OrderBacklogProjection> projections = orderRepository.findBacklogOrders(BACKLOG_STATUSES, PageRequest.of(0, limit));
        List<OrderBacklogResponse.OrderSummary> summaries = projections.stream()
                .map(this::toBacklogSummary)
                .toList();

        return OrderBacklogResponse.builder()
                .totalPending(totalPending)
                .initialCount(initialCount)
                .processingCount(processingCount)
                .oldestOrders(summaries)
                .build();
    }

    @Override
    public TestDepartmentStatsResponse getDepartmentStats(LocalDate fromDate, LocalDate toDate, Long departmentId) {
        loggerService.info("dashboard_metrics -> department stats from {} to {} for department {}",
                fromDate, toDate, departmentId);
        Instant from = startOfDay(fromDate);
        Instant to = endOfDayExclusive(toDate);

        List<TestDepartmentAggregation> aggregations = testResultRepository.aggregateByDepartment(from, to, departmentId);
        List<TestDepartmentStatsResponse.DepartmentStats> stats = aggregations.stream()
                .map(aggregation -> TestDepartmentStatsResponse.DepartmentStats.builder()
                        .departmentId(aggregation.getDepartmentId())
                        .departmentName(Optional.ofNullable(aggregation.getDepartmentName()).orElse("UNASSIGNED"))
                        .totalTests(aggregation.getTotalTests())
                        .pendingResults(aggregation.getPendingResults())
                        .finalizedResults(aggregation.getFinalizedResults())
                        .abnormalResults(aggregation.getAbnormalResults())
                        .build())
                .sorted(Comparator.comparingLong(TestDepartmentStatsResponse.DepartmentStats::getTotalTests).reversed())
                .toList();

        return TestDepartmentStatsResponse.builder()
                .departments(stats)
                .build();
    }

    @Override
    public TestTatStatsResponse getTatStats(LocalDate fromDate, LocalDate toDate, Long departmentId) {
        loggerService.info("dashboard_metrics -> TAT stats from {} to {} for department {}",
                fromDate, toDate, departmentId);
        List<TestTatProjection> projections = testResultRepository.findTatCandidates(fromDate, toDate, departmentId);
        Map<TatGroupKey, List<Long>> durationsByGroup = new LinkedHashMap<>();

        for (TestTatProjection projection : projections) {
            if (projection.getCollectedAt() == null || projection.getResultUpdatedAt() == null) {
                continue;
            }
            Instant collectedInstant = projection.getCollectedAt().atZone(SYSTEM_ZONE).toInstant();
            long seconds = Duration.between(collectedInstant, projection.getResultUpdatedAt()).getSeconds();
            if (seconds < 0) {
                continue;
            }
            TatGroupKey key = new TatGroupKey(
                    projection.getDepartmentId(),
                    Optional.ofNullable(projection.getDepartmentName()).orElse("UNASSIGNED"),
                    projection.getTestCode(),
                    projection.getTestName()
            );
            durationsByGroup.computeIfAbsent(key, ignored -> new ArrayList<>()).add(seconds);
        }

        List<TestTatStatsResponse.TatMetric> metrics = durationsByGroup.entrySet().stream()
                .map(entry -> {
                    TatGroupKey key = entry.getKey();
                    List<Long> durations = entry.getValue();
                    long sampleSize = durations.size();
                    return TestTatStatsResponse.TatMetric.builder()
                            .departmentId(key.departmentId())
                            .departmentName(key.departmentName())
                            .testCode(key.testCode())
                            .testName(key.testName())
                            .sampleSize(sampleSize)
                            .averageTat(averageDuration(durations))
                            .medianTat(percentileDuration(durations, 0.5))
                            .percentile90Tat(percentileDuration(durations, 0.9))
                            .build();
                })
                .sorted(Comparator.comparingLong(TestTatStatsResponse.TatMetric::getSampleSize).reversed())
                .toList();

        return TestTatStatsResponse.builder()
                .metrics(metrics)
                .build();
    }

    @Override
    public SmsSummaryResponse getSmsSummary(LocalDate fromDate, LocalDate toDate) {
        loggerService.info("dashboard_metrics -> sms summary from {} to {}", fromDate, toDate);
        LocalDateTime smsFrom = startOfDayLocal(fromDate);
        LocalDateTime smsTo = endOfDayExclusiveLocal(toDate);

        long totalSent = smsHistoryRepository.countSentBetween(smsFrom, smsTo);
        long totalDelivered = smsHistoryRepository.countDeliveredBetween(smsFrom, smsTo);
        long totalFailed = smsHistoryRepository.countFailedBetween(smsFrom, smsTo);
        BigDecimal deliveryRate = computeRate(totalDelivered, totalSent);
        Double avgLatencySeconds = smsHistoryRepository.averageDeliveryLatency(smsFrom, smsTo);

        return SmsSummaryResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalSent(totalSent)
                .totalDelivered(totalDelivered)
                .totalFailed(totalFailed)
                .deliveryRate(deliveryRate)
                .averageDeliveryLatencySeconds(avgLatencySeconds == null ? BigDecimal.ZERO :
                        BigDecimal.valueOf(avgLatencySeconds).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    public SmsErrorBreakdownResponse getSmsErrorBreakdown(LocalDate fromDate, LocalDate toDate, int limit) {
        loggerService.info("dashboard_metrics -> sms error breakdown from {} to {} limit {}", fromDate, toDate, limit);
        LocalDateTime smsFrom = startOfDayLocal(fromDate);
        LocalDateTime smsTo = endOfDayExclusiveLocal(toDate);

        List<SmsErrorAggregation> aggregations = smsHistoryRepository.aggregateErrors(smsFrom, smsTo, limit);
        List<SmsErrorBreakdownResponse.ErrorItem> errors = aggregations.stream()
                .map(aggregation -> SmsErrorBreakdownResponse.ErrorItem.builder()
                        .statusCode(aggregation.getStatusCode())
                        .errorMessage(Optional.ofNullable(aggregation.getErrorMessage()).orElse("UNKNOWN"))
                        .occurrences(aggregation.getOccurrences())
                        .mostRecentOccurrence(formatInstant(aggregation.getMostRecentOccurrence()))
                        .build())
                .toList();

        return SmsErrorBreakdownResponse.builder()
                .errors(errors)
                .build();
    }

    @Override
    public PatientNotificationIssueResponse getPatientNotificationIssues(LocalDate fromDate, LocalDate toDate, long threshold) {
        loggerService.info("dashboard_metrics -> patient notification issues from {} to {} threshold {}",
                fromDate, toDate, threshold);
        LocalDateTime smsFrom = startOfDayLocal(fromDate);
        LocalDateTime smsTo = endOfDayExclusiveLocal(toDate);

        List<PatientNotificationIssueProjection> projections = smsHistoryRepository.findPatientIssues(smsFrom, smsTo, threshold);
        List<PatientNotificationIssueResponse.Issue> issues = projections.stream()
                .map(projection -> PatientNotificationIssueResponse.Issue.builder()
                        .patientId(projection.getPatientId())
                        .mrNumber(projection.getMrNumber())
                        .patientName(projection.getPatientName())
                        .phoneNumber(projection.getPhoneNumber())
                        .failedAttempts(projection.getFailedAttempts())
                        .missingPhoneCount(projection.getMissingPhoneCount())
                        .lastFailure(formatInstant(projection.getLastFailure()))
                        .build())
                .toList();

        return PatientNotificationIssueResponse.builder()
                .issues(issues)
                .build();
    }

    private Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(SYSTEM_ZONE).toInstant();
    }

    private Instant endOfDayExclusive(LocalDate date) {
        return date.plusDays(1).atStartOfDay(SYSTEM_ZONE).toInstant();
    }

    private LocalDateTime startOfDayLocal(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDayExclusiveLocal(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    private long sumCounts(Map<LocalDate, Long> counts, LocalDate from, LocalDate to) {
        long total = 0;
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            total += counts.getOrDefault(cursor, 0L);
            cursor = cursor.plusDays(1);
        }
        return total;
    }

    private OrderBacklogResponse.OrderSummary toBacklogSummary(OrderBacklogProjection projection) {
        return OrderBacklogResponse.OrderSummary.builder()
                .orderId(projection.getId())
                .orderNumber(projection.getOrderNumber())
                .orderStatus(projection.getOrderStatus())
                .resultStatus(projection.getResultStatus())
                .orderType(projection.getOrderType())
                .patientMrn(projection.getPatientMrNumber())
                .patientName(projection.getPatientName())
                .collectedAt(formatLocalDateTime(projection.getCollectedAt()))
                .createdAt(formatInstant(projection.getCreatedAt()))
                .build();
    }

    private Duration averageDuration(List<Long> seconds) {
        if (seconds == null || seconds.isEmpty()) {
            return Duration.ZERO;
        }
        long sum = seconds.stream().mapToLong(Long::longValue).sum();
        long avg = Math.round((double) sum / seconds.size());
        return Duration.ofSeconds(Math.max(avg, 0));
    }

    private Duration percentileDuration(List<Long> seconds, double percentile) {
        if (seconds == null || seconds.isEmpty()) {
            return Duration.ZERO;
        }
        List<Long> sorted = new ArrayList<>(seconds);
        sorted.sort(Long::compareTo);
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return Duration.ofSeconds(Math.max(sorted.get(index), 0));
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : ISO_INSTANT.format(instant);
    }

    private String formatLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : ISO_LOCAL_DATE_TIME.format(dateTime);
    }

    private BigDecimal computeRate(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private record TatGroupKey(Long departmentId, String departmentName, String testCode, String testName) {
    }
}
