package com.muhimbili.labnotification.service;

import com.google.gson.Gson;
import com.muhimbili.labnotification.configation.database.DatabaseRepository;
import com.muhimbili.labnotification.configation.database.entities.Batch;
import com.muhimbili.labnotification.configation.database.entities.LabDepartment;
import com.muhimbili.labnotification.configation.database.entities.Order;
import com.muhimbili.labnotification.configation.database.entities.Patient;
import com.muhimbili.labnotification.configation.database.entities.Profile;
import com.muhimbili.labnotification.configation.database.entities.TestResult;
import com.muhimbili.labnotification.data.response.ExternalLabResultsResponse;
import com.muhimbili.labnotification.utility.LoggerService;
import com.muhimbili.labnotification.utility.StringUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class LabResultsPersistenceService {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.BASIC_ISO_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_TIME,
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HHmm")
    );

    private final DatabaseRepository databaseRepository;
    private final StringUtility stringUtility;
    private final LoggerService loggerService;
    private final Gson gson;

    public LabResultsPersistenceService(DatabaseRepository databaseRepository,
                                        StringUtility stringUtility,
                                        LoggerService loggerService,
                                        Gson gson) {
        this.databaseRepository = databaseRepository;
        this.stringUtility = stringUtility;
        this.loggerService = loggerService;
        this.gson = gson;
    }

    @Transactional
    public void persist(ExternalLabResultsResponse externalResponse) {
        if (externalResponse == null || externalResponse.getData() == null) {
            loggerService.warn("lab_results_persistence -> no payload to persist");
            return;
        }

        ExternalLabResultsResponse.DataPayload payload = externalResponse.getData();
        List<ExternalLabResultsResponse.LabResultItem> labResults = payload.getLabResults();
        if (labResults == null || labResults.isEmpty()) {
            loggerService.warn("lab_results_persistence -> payload contains no lab results");
            return;
        }

        Batch batch = resolveBatch(payload, externalResponse.getStatus());
        Map<String, Patient> patients = new HashMap<>();
        Map<String, Order> orders = new HashMap<>();
        Map<String, Profile> profiles = new HashMap<>();
        Map<String, LabDepartment> labDepartments = new HashMap<>();

        for (ExternalLabResultsResponse.LabResultItem item : labResults) {
            if (item == null || !StringUtils.hasText(item.getOrderNum())) {
                continue;
            }

            String orderNumber = item.getOrderNum();
            if (databaseRepository.orderExists(orderNumber)) {
                loggerService.debug("lab_results_persistence -> skipping duplicate order {}", orderNumber);
                continue;
            }

            Patient patient = resolvePatient(item, patients);
            Order order = resolveOrder(item, patient, batch, 100, orders);
            Profile profile = resolveProfile(item, order, profiles);
            LabDepartment labDepartment = resolveLabDepartment(item, labDepartments);
            persistTest(item, order, profile, labDepartment);
        }

        loggerService.info("lab_results_persistence -> persisted {} patients, {} orders, {} tests", 
                patients.size(), orders.size(), labResults.size());
    }

    private Batch resolveBatch(ExternalLabResultsResponse.DataPayload payload, int statusCode) {
        LocalDate resultsDate = safeDate(payload.getResultsDate());
        LocalTime fromTime = safeTime(payload.getFromTime());
        LocalTime toTime = safeTime(payload.getToTime());
        String status = StringUtils.hasText(payload.getProcessingStatus()) ? payload.getProcessingStatus() : "UNKNOWN";
        String payloadHash = stringUtility.hashString(gson.toJson(payload));
        Instant now = Instant.now();

        Batch batch = databaseRepository.findBatch(resultsDate, fromTime, toTime, status)
                .orElseGet(Batch::new);

        batch.setResultsDate(resultsDate);
        batch.setFromTime(fromTime);
        batch.setToTime(toTime);
        batch.setStatus(status);
        batch.setFetchedAt(now);
        batch.setPayloadHash(payloadHash + statusCode);
        return databaseRepository.saveBatch(batch);
    }

    private Patient resolvePatient(ExternalLabResultsResponse.LabResultItem item, Map<String, Patient> cache) {
        Instant now = Instant.now();
        String mrNumber = buildMrNumber(item);
        return cache.computeIfAbsent(mrNumber, key -> {
            Optional<Patient> existing = databaseRepository.findPatientByMrNumber(key);
            Patient patient = existing.orElseGet(() -> {
                Patient p = new Patient();
                p.setMrNumber(key);
                p.setCreatedAt(now);
                return p;
            });

            patient.setPatientName(defaultString(item.getPatientName(), key));
            patient.setPhoneNumber(item.getPatMobile());
            patient.setUpdatedAt(now);
            Patient saved = databaseRepository.savePatient(patient);
            return saved;
        });
    }

    private Order resolveOrder(ExternalLabResultsResponse.LabResultItem item,
                               Patient patient,
                               Batch batch,
                               int statusCode,
                               Map<String, Order> cache) {
        Instant now = Instant.now();
        String orderNum = Objects.requireNonNull(item.getOrderNum());

        return cache.computeIfAbsent(orderNum, key -> {
            Optional<Order> existing = databaseRepository.findOrderByNumber(key);
            Order order = existing.orElseGet(() -> {
                Order o = new Order();
                o.setOrderNumber(key);
                o.setCreatedAt(now);
                return o;
            });

            order.setPatient(patient);
            order.setBatch(batch);
            order.setOrderDate(safeDate(item.getOrderDate()));
            order.setOrderTime(safeTime(item.getOrderTime()));
            order.setCollectedAt(safeDateTime(item.getOrderDate(), item.getOrderTime()));
            order.setOrderStatus(defaultString(item.getOrderStatus(), "PENDING"));
            order.setResultStatus(defaultString(item.getResultStatus(), "PENDING"));
            order.setOrderType(defaultString(item.getOrderType(), "GEN"));
            order.setStatus(Order.ProcessingStatus.PROCESSING);
            order.setStatusCode(statusCode);
            order.setUpdatedAt(now);
            return databaseRepository.saveOrder(order);
        });
    }

    private Profile resolveProfile(ExternalLabResultsResponse.LabResultItem item,
                                   Order order,
                                   Map<String, Profile> cache) {
        String profileCode = StringUtils.hasText(item.getProfileCode()) ? item.getProfileCode() : item.getTestProfile();
        if (!StringUtils.hasText(profileCode)) {
            return null;
        }

        Instant now = Instant.now();
        String cacheKey = order.getOrderNumber() + "|" + profileCode;

        return cache.computeIfAbsent(cacheKey, key -> {
            Optional<Profile> existing = order.getId() == null ? Optional.empty()
                    : databaseRepository.findProfileByOrderAndCode(order.getId(), profileCode);

            Profile profile = existing.orElseGet(() -> {
                Profile p = new Profile();
                p.setOrder(order);
                p.setProfileCode(profileCode);
                p.setCreatedAt(now);
                return p;
            });

            profile.setOrder(order);
            profile.setProfileCode(profileCode);
            profile.setUpdatedAt(now);
            return databaseRepository.saveProfile(profile);
        });
    }

    private LabDepartment resolveLabDepartment(ExternalLabResultsResponse.LabResultItem item,
                                               Map<String, LabDepartment> cache) {
        if (!StringUtils.hasText(item.getLabDeptcode()) && !StringUtils.hasText(item.getLabCode())) {
            return null;
        }

        Instant now = Instant.now();
        String code = defaultString(item.getLabDeptcode(), "LAB" + defaultString(item.getLabCode(), "0000"));
        String labCode = defaultString(item.getLabCode(), code);
        String cacheKey = code + "|" + labCode;

        return cache.computeIfAbsent(cacheKey, key -> {
            Optional<LabDepartment> existing = databaseRepository.findLabDepartmentByCode(code, labCode);
            LabDepartment department = existing.orElseGet(() -> {
                LabDepartment d = new LabDepartment();
                d.setCode(code);
                d.setLabCode(labCode);
                d.setCreatedAt(now);
                return d;
            });

            department.setDescription(item.getLabdeptDesc());
            department.setLabName(item.getLabDesc());
            department.setLabType(item.getLabType());
            department.setUpdatedAt(now);
            department.setLabCode(labCode);
            return databaseRepository.saveLabDepartment(department);
        });
    }

    private void persistTest(ExternalLabResultsResponse.LabResultItem item,
                             Order order,
                             Profile profile,
                             LabDepartment labDepartment) {
        Instant now = Instant.now();
        if (!StringUtils.hasText(item.getTestCode())) {
            return;
        }

        Optional<TestResult> existing = order.getId() == null ? Optional.empty()
                : databaseRepository.findTestByOrderAndCode(order.getId(), item.getTestCode());

        TestResult test = existing.orElseGet(() -> {
            TestResult t = new TestResult();
            t.setOrder(order);
            t.setTestCode(item.getTestCode());
            t.setCreatedAt(now);
            return t;
        });

        test.setOrder(order);
        test.setProfile(profile);
        test.setLabDepartment(labDepartment);
        test.setTestName(defaultString(item.getTestName(), item.getTestCode()));
        test.setResultStatus(defaultString(item.getResultStatus(), order.getResultStatus()));
        test.setOrderStatus(defaultString(item.getOrderStatus(), order.getOrderStatus()));
        test.setOrderType(defaultString(item.getOrderType(), order.getOrderType()));
        test.setUpdatedAt(now);
        databaseRepository.saveTest(test);
    }

    private String buildMrNumber(ExternalLabResultsResponse.LabResultItem item) {
        if (StringUtils.hasText(item.getMrNumber())) {
            return item.getMrNumber();
        }
        String suffix = StringUtils.hasText(item.getOrderNum()) ? item.getOrderNum() : stringUtility.generateRandomId();
        return "UNKNOWN-" + suffix;
    }

    private LocalDate safeDate(String value) {
        if (!StringUtils.hasText(value)) {
            return LocalDate.now();
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return LocalDate.now();
    }

    private LocalTime safeTime(String value) {
        if (!StringUtils.hasText(value)) {
            return LocalTime.MIDNIGHT;
        }
        for (DateTimeFormatter formatter : TIME_FORMATS) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return LocalTime.MIDNIGHT;
    }

    private LocalDateTime safeDateTime(String date, String time) {
        LocalDate parsedDate = safeDate(date);
        LocalTime parsedTime = safeTime(time);
        return LocalDateTime.of(parsedDate, parsedTime);
    }

    private String defaultString(String candidate, String defaultValue) {
        return StringUtils.hasText(candidate) ? candidate : defaultValue;
    }
}
