package com.muhimbili.labnotification.configation.database;

import com.muhimbili.labnotification.configation.database.entities.Batch;
import com.muhimbili.labnotification.configation.database.entities.LabDepartment;
import com.muhimbili.labnotification.configation.database.entities.LabResultWindow;
import com.muhimbili.labnotification.configation.database.entities.Order;
import com.muhimbili.labnotification.configation.database.entities.Patient;
import com.muhimbili.labnotification.configation.database.entities.Profile;
import com.muhimbili.labnotification.configation.database.entities.SmsHistory;
import com.muhimbili.labnotification.configation.database.entities.TestResult;
import com.muhimbili.labnotification.configation.database.projectors.BatchProjector;
import com.muhimbili.labnotification.configation.database.projectors.LabDepartmentProjector;
import com.muhimbili.labnotification.configation.database.projectors.LabResultWindowProjector;
import com.muhimbili.labnotification.configation.database.projectors.OrderProjector;
import com.muhimbili.labnotification.configation.database.projectors.PatientProjector;
import com.muhimbili.labnotification.configation.database.projectors.ProfileProjector;
import com.muhimbili.labnotification.configation.database.projectors.SmsHistoryProjector;
import com.muhimbili.labnotification.configation.database.projectors.SmsOrderProjection;
import com.muhimbili.labnotification.configation.database.projectors.TestResultProjector;
import com.muhimbili.labnotification.configation.database.repository.BatchRepository;
import com.muhimbili.labnotification.configation.database.repository.LabDepartmentRepository;
import com.muhimbili.labnotification.configation.database.repository.LabResultWindowRepository;
import com.muhimbili.labnotification.configation.database.repository.OrderRepository;
import com.muhimbili.labnotification.configation.database.repository.PatientRepository;
import com.muhimbili.labnotification.configation.database.repository.ProfileRepository;
import com.muhimbili.labnotification.configation.database.repository.SmsHistoryRepository;
import com.muhimbili.labnotification.configation.database.repository.TestResultRepository;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Convenience facade that centralizes access to all persistence repositories.
 * This helps keep service-layer code tidy while allowing projections to be
 * reused from a single place.
 */
@Service
@Getter
public class DatabaseRepository {

    private final BatchRepository batchRepository;
    private final PatientRepository patientRepository;
    private final LabDepartmentRepository labDepartmentRepository;
    private final OrderRepository orderRepository;
    private final ProfileRepository profileRepository;
    private final TestResultRepository testResultRepository;
    private final LabResultWindowRepository labResultWindowRepository;
    private final SmsHistoryRepository smsHistoryRepository;

    public DatabaseRepository(BatchRepository batchRepository,
                              PatientRepository patientRepository,
                              LabDepartmentRepository labDepartmentRepository,
                              OrderRepository orderRepository,
                              ProfileRepository profileRepository,
                              TestResultRepository testResultRepository,
                              LabResultWindowRepository labResultWindowRepository,
                              SmsHistoryRepository smsHistoryRepository) {
        this.batchRepository = batchRepository;
        this.patientRepository = patientRepository;
        this.labDepartmentRepository = labDepartmentRepository;
        this.orderRepository = orderRepository;
        this.profileRepository = profileRepository;
        this.testResultRepository = testResultRepository;
        this.labResultWindowRepository = labResultWindowRepository;
        this.smsHistoryRepository = smsHistoryRepository;
    }

    // ---------------------------------------------------------------------
    // Batch helpers
    // ---------------------------------------------------------------------
    public Batch saveBatch(Batch batch) {
        return batchRepository.save(batch);
    }

    public Optional<Batch> findBatchById(Long id) {
        return batchRepository.findById(id);
    }

    public Optional<Batch> findBatch(LocalDate resultsDate, LocalTime fromTime, LocalTime toTime, String status) {
        return batchRepository.findByResultsDateAndFromTimeAndToTimeAndStatus(resultsDate, fromTime, toTime, status);
    }

    public BatchProjector getBatchProjection(Long id) {
        return batchRepository.findProjectedById(id);
    }

    // ---------------------------------------------------------------------
    // Patient helpers
    // ---------------------------------------------------------------------
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Optional<Patient> findPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Optional<Patient> findPatientByMrNumber(String mrNumber) {
        return patientRepository.findByMrNumber(mrNumber);
    }

    public PatientProjector getPatientProjection(Long id) {
        return patientRepository.findProjectedById(id);
    }

    // ---------------------------------------------------------------------
    // Lab Department helpers
    // ---------------------------------------------------------------------
    public LabDepartment saveLabDepartment(LabDepartment department) {
        return labDepartmentRepository.save(department);
    }

    public Optional<LabDepartment> findLabDepartmentById(Long id) {
        return labDepartmentRepository.findById(id);
    }

    public Optional<LabDepartment> findLabDepartmentByCode(String code, String labCode) {
        return labDepartmentRepository.findByCodeAndLabCode(code, labCode);
    }

    public LabDepartmentProjector getLabDepartmentProjection(Long id) {
        return labDepartmentRepository.findProjectedById(id);
    }

    // ---------------------------------------------------------------------
    // Orders
    // ---------------------------------------------------------------------
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public boolean orderExists(String orderNumber) {
        return orderRepository.existsByOrderNumber(orderNumber);
    }

    public OrderProjector getOrderProjection(String orderNumber) {
        return orderRepository.findProjectedByOrderNumber(orderNumber);
    }

    public List<SmsOrderProjection> findSmsOrderCandidates(List<String> resultStatuses, int statusCode, int limit) {
        return orderRepository.findSmsCandidates(resultStatuses, statusCode, PageRequest.of(0, limit));
    }

    public void updateOrderStatuses(List<Long> orderIds, Order.ProcessingStatus status, int statusCode) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        orderRepository.updateStatusForIds(status, statusCode, orderIds);
    }

    // ---------------------------------------------------------------------
    // Profiles
    // ---------------------------------------------------------------------
    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    public Optional<Profile> findProfileById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> findProfileByOrderAndCode(Long orderId, String profileCode) {
        return profileRepository.findByOrderIdAndProfileCode(orderId, profileCode);
    }

    public ProfileProjector getProfileProjection(Long id) {
        return profileRepository.findProjectedById(id);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------
    public TestResult saveTest(TestResult testResult) {
        return testResultRepository.save(testResult);
    }

    public Optional<TestResult> findTestById(Long id) {
        return testResultRepository.findById(id);
    }

    public Optional<TestResult> findTestByOrderAndCode(Long orderId, String testCode) {
        return testResultRepository.findByOrderIdAndTestCode(orderId, testCode);
    }

    public List<TestResult> findTestsByOrderIds(List<Long> orderIds) {
        return orderIds == null || orderIds.isEmpty() ? List.of() : testResultRepository.findAllByOrderIds(orderIds);
    }

    public TestResultProjector getTestProjection(Long id) {
        return testResultRepository.findProjectedById(id);
    }

    // ---------------------------------------------------------------------
    // Lab result windows
    // ---------------------------------------------------------------------
    public LabResultWindow saveLabResultWindow(LabResultWindow window) {
        return labResultWindowRepository.save(window);
    }

    public Optional<LabResultWindow> findLabResultWindowById(Long id) {
        return labResultWindowRepository.findById(id);
    }

    public Optional<LabResultWindow> findLabResultWindow(String resultsDate, LocalTime fromTime) {
        return labResultWindowRepository.findByResultsDateAndFromTime(resultsDate, fromTime);
    }

    public LabResultWindowProjector getLabResultWindowProjection(Long id) {
        return labResultWindowRepository.findProjectedById(id);
    }

    // ---------------------------------------------------------------------
    // SMS History
    // ---------------------------------------------------------------------
    public SmsHistory saveSmsHistory(SmsHistory smsHistory) {
        return smsHistoryRepository.save(smsHistory);
    }

    public Optional<SmsHistory> findSmsHistoryById(Long id) {
        return smsHistoryRepository.findById(id);
    }

    public List<SmsHistoryProjector> getSmsHistoryByPhone(String phoneNumber) {
        return smsHistoryRepository.findByPhoneNumber(phoneNumber);
    }

    public List<SmsHistory> findPendingSmsHistories(int statusCode, int limit) {
        return smsHistoryRepository.findPendingForDispatch(statusCode, PageRequest.of(0, limit));
    }

    public void updateSmsHistoryStatus(Long id, String status, int statusCode, String providerId, String errorMessage) {
        smsHistoryRepository.updateStatus(id, status, statusCode, providerId, errorMessage);
    }
}
