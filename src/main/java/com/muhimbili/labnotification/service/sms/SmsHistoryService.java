package com.muhimbili.labnotification.service.sms;

import com.muhimbili.labnotification.configation.database.DatabaseRepository;
import com.muhimbili.labnotification.configation.database.entities.Order;
import com.muhimbili.labnotification.configation.database.entities.Patient;
import com.muhimbili.labnotification.configation.database.entities.SmsHistory;
import com.muhimbili.labnotification.configation.database.entities.TestResult;
import com.muhimbili.labnotification.configation.database.projectors.SmsOrderProjection;
import com.muhimbili.labnotification.service.sms.client.KilakonaMessageResponse;
import com.muhimbili.labnotification.service.sms.client.KilakonaSmsClient;
import com.muhimbili.labnotification.utility.ApplicationProp;
import com.muhimbili.labnotification.utility.LoggerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SmsHistoryService {

    private static final String RESULT_RELEASED = "R";
    private static final String RESULT_VALIDATED = "V";
    private static final String SMS_STATUS_FAILED = "FAILED";
    private static final String SMS_STATUS_PROCESSING = "PROCESSING";
    private static final String SMS_STATUS_SENT = "SENT";
    private static final int ORDER_STATUS_PENDING = 100;
    private static final int ORDER_STATUS_PROCESSING = 200;
    private static final int SMS_STATUS_CODE_PROCESSING = 200;
    private static final int SMS_STATUS_CODE_SENT = 300;
    private static final int SMS_STATUS_CODE_FAILED = 400;
    private static final String NO_PHONE_PLACEHOLDER = "NO_PHONE";
    private static final int DISPATCH_BATCH_LIMIT = 50;
    private static final String DEFAULT_PATIENT_NAME = "Mgonjwa";

    private final DatabaseRepository databaseRepository;
    private final ApplicationProp applicationProp;
    private final LoggerService loggerService;
    private final KilakonaSmsClient kilakonaSmsClient;

    public SmsHistoryService(DatabaseRepository databaseRepository,
                             ApplicationProp applicationProp,
                             LoggerService loggerService,
                             KilakonaSmsClient kilakonaSmsClient) {
        this.databaseRepository = databaseRepository;
        this.applicationProp = applicationProp;
        this.loggerService = loggerService;
        this.kilakonaSmsClient = kilakonaSmsClient;
    }

    @Transactional
    public List<SmsHistory> stageSmsHistoryBatch() {
        List<SmsOrderProjection> candidates = databaseRepository.findSmsOrderCandidates(
                List.of(RESULT_RELEASED, RESULT_VALIDATED), ORDER_STATUS_PENDING, applicationProp.getSmsBatchSize());

        if (candidates.isEmpty()) {
            loggerService.debug("sms_history -> no orders awaiting sms notifications");
            return List.of();
        }

        loggerService.info("sms_history -> fetched {} candidate orders", candidates.size());

        List<Long> orderIds = candidates.stream().map(SmsOrderProjection::getId).toList();
        databaseRepository.updateOrderStatuses(orderIds, Order.ProcessingStatus.PROCESSING, ORDER_STATUS_PROCESSING);
        loggerService.debug("sms_history -> marked orders {} as PROCESSING", orderIds);

        Map<Long, List<TestResult>> testsByOrder = databaseRepository.findTestsByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(test -> test.getOrder().getId()));
        loggerService.debug("sms_history -> loaded tests for {} orders", testsByOrder.size());

        List<SmsHistory> stagedEntries = new ArrayList<>();
        for (SmsOrderProjection candidate : candidates) {
            List<TestResult> tests = testsByOrder.get(candidate.getId());
            if (!hasReleasedTests(tests)) {
                loggerService.warn("sms_history -> order {} skipped because no released tests", candidate.getOrderNumber());
                continue;
            }

            SmsHistory smsHistory = buildSmsHistory(candidate, tests);
            SmsHistory saved = databaseRepository.saveSmsHistory(smsHistory);
            loggerService.debug("sms_history -> staged sms entry id={} order={}", saved.getId(), saved.getNotificationId());
            stagedEntries.add(saved);
        }

        loggerService.info("sms_history -> staged {} entries", stagedEntries.size());
        return stagedEntries;
    }

    @Transactional
    public int dispatchPendingSms() {
        List<SmsHistory> pending = databaseRepository.findPendingSmsHistories(SMS_STATUS_CODE_PROCESSING, DISPATCH_BATCH_LIMIT);
        if (pending.isEmpty()) {
            loggerService.debug("sms_history -> no queued sms awaiting dispatch");
            return 0;
        }

        loggerService.info("sms_history -> dispatching {} queued sms", pending.size());
        int successCount = 0;
        for (SmsHistory smsHistory : pending) {
            loggerService.debug("sms_history -> sending notification={} phone={}",
                    smsHistory.getNotificationId(), smsHistory.getPhoneNumber());
            if (!StringUtils.hasText(smsHistory.getPhoneNumber()) || smsHistory.getPhoneNumber().startsWith("000")) {
                loggerService.warn("sms_history -> invalid phone for notification={} phone={}",
                        smsHistory.getNotificationId(), smsHistory.getPhoneNumber());
                databaseRepository.updateSmsHistoryStatus(smsHistory.getId(), SMS_STATUS_FAILED, SMS_STATUS_CODE_FAILED,
                        smsHistory.getProviderMessageId(), "Invalid phone number");
                continue;
            }

            boolean sent = dispatchSmsIfPossible(smsHistory);
            if (sent) {
                successCount++;
            }
        }
        loggerService.info("sms_history -> dispatched {} / {} sms", successCount, pending.size());
        return successCount;
    }

    private boolean hasReleasedTests(List<TestResult> tests) {
        if (CollectionUtils.isEmpty(tests)) {
            return false;
        }
        return tests.stream().anyMatch(test -> {
            String status = test.getResultStatus();
            return RESULT_RELEASED.equalsIgnoreCase(status) || RESULT_VALIDATED.equalsIgnoreCase(status);
        });
    }

    private SmsHistory buildSmsHistory(SmsOrderProjection orderProjection, List<TestResult> tests) {
        Instant now = Instant.now();
        SmsHistory smsHistory = new SmsHistory();
        smsHistory.setNotificationId(orderProjection.getOrderNumber());
        smsHistory.setPatient(referencePatient(orderProjection));
        String phoneNumber = resolvePhone(orderProjection);
        smsHistory.setPhoneNumber(StringUtils.hasText(phoneNumber) ? phoneNumber : NO_PHONE_PLACEHOLDER);
        smsHistory.setMessageBody(buildMessageCustom(orderProjection));
        smsHistory.setSentAt(LocalDateTime.now());
        smsHistory.setCreatedAt(now);
        smsHistory.setUpdatedAt(now);
        smsHistory.setProviderMessageId(applicationProp.getSmsProviderId());

        if (!StringUtils.hasText(phoneNumber)) {
            smsHistory.setStatus(SMS_STATUS_FAILED);
            smsHistory.setStatusCode(SMS_STATUS_CODE_FAILED);
            smsHistory.setErrorMessage("No phone number");
        } else {
            smsHistory.setStatus(SMS_STATUS_PROCESSING);
            smsHistory.setStatusCode(SMS_STATUS_CODE_PROCESSING);
        }
        return smsHistory;
    }

    private boolean dispatchSmsIfPossible(SmsHistory smsHistory) {
        if (!StringUtils.hasText(smsHistory.getPhoneNumber()) || smsHistory.getPhoneNumber().startsWith("000")) {
            loggerService.warn("sms_history -> skipping sms send for order {} due to invalid phone", smsHistory.getNotificationId());
            databaseRepository.updateSmsHistoryStatus(smsHistory.getId(), SMS_STATUS_FAILED, SMS_STATUS_CODE_FAILED,
                    smsHistory.getProviderMessageId(), "Invalid phone");
            return false;
        }

        KilakonaMessageResponse response = kilakonaSmsClient.sendText(
                smsHistory.getMessageBody(),
                List.of(smsHistory.getPhoneNumber())
        );

        boolean success = response != null && response.isSuccessful();
        String providerId = success && response.data() != null ? response.data().shootId() : smsHistory.getProviderMessageId();
        String error = success ? null : (response == null ? "Gateway error" : response.message());
        int statusCode = success ? SMS_STATUS_CODE_SENT : SMS_STATUS_CODE_FAILED;
        String status = success ? SMS_STATUS_SENT : SMS_STATUS_FAILED;

        databaseRepository.updateSmsHistoryStatus(smsHistory.getId(), status, statusCode, providerId, error);
        return success;
    }

    private Patient referencePatient(SmsOrderProjection orderProjection) {
        SmsOrderProjection.OrderPatientProjection patientProjection = orderProjection.getPatient();
        if (patientProjection == null || patientProjection.getId() == null) {
            return null;
        }
        Patient patient = new Patient();
        patient.setId(patientProjection.getId());
        return patient;
    }

    private String resolvePhone(SmsOrderProjection orderProjection) {
        SmsOrderProjection.OrderPatientProjection patientProjection = orderProjection.getPatient();
        return patientProjection == null ? null : patientProjection.getPhoneNumber();
    }

    private String buildMessageCustom(SmsOrderProjection orderProjection) {
        String template = applicationProp.getSmsMessageTemplateSwahili();

        SmsOrderProjection.OrderPatientProjection patientProjection = orderProjection.getPatient();
        String rawPatientName = patientProjection != null ? patientProjection.getPatientName() : null;
        String patientName = normalisePatientName(rawPatientName);

        String message = String.format(template, patientName);
        loggerService.info("sms_history -> sms swahili template={}", message);
        return message;
    }

    private String normalisePatientName(String patientName) {
        if (!StringUtils.hasText(patientName)) {
            return DEFAULT_PATIENT_NAME;
        }

        return Arrays.stream(patientName.trim().split("\\s+"))
                .filter(StringUtils::hasText)
                .map(segment -> {
                    String lower = segment.toLowerCase(Locale.ROOT);
                    return lower.length() == 1
                            ? lower.toUpperCase(Locale.ROOT)
                            : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
                })
                .collect(Collectors.joining(" "));
    }

    private String summariseTests(List<TestResult> tests) {
        if (CollectionUtils.isEmpty(tests)) {
            return "";
        }
        Set<String> names = tests.stream()
                .map(TestResult::getTestName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return String.join(", ", names);
    }
}
