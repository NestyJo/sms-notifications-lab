package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.SmsHistory;
import com.muhimbili.labnotification.configation.database.projectors.PatientNotificationIssueProjection;
import com.muhimbili.labnotification.configation.database.projectors.SmsErrorAggregation;
import com.muhimbili.labnotification.configation.database.projectors.SmsHistoryProjector;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SmsHistoryRepository extends JpaRepository<SmsHistory, Long> {
    List<SmsHistoryProjector> findByPhoneNumber(String phoneNumber);

    @Query("SELECT sh FROM SmsHistory sh WHERE sh.statusCode = :statusCode " +
            "AND sh.phoneNumber IS NOT NULL AND sh.phoneNumber <> '' " +
            "AND sh.phoneNumber NOT LIKE '000%' ORDER BY sh.id ASC")
    List<SmsHistory> findPendingForDispatch(@Param("statusCode") Integer statusCode, Pageable pageable);

    @Modifying
    @Query("UPDATE SmsHistory sh SET sh.status = :status, sh.statusCode = :statusCode, " +
            "sh.providerMessageId = :providerMessageId, sh.errorMessage = :errorMessage, sh.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE sh.id = :id")
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("statusCode") Integer statusCode,
                     @Param("providerMessageId") String providerMessageId,
                     @Param("errorMessage") String errorMessage);

    @Query("SELECT COUNT(sh) FROM SmsHistory sh WHERE sh.sentAt BETWEEN :from AND :to")
    long countSentBetween(@Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(sh) FROM SmsHistory sh WHERE sh.status = 'DELIVERED' AND sh.sentAt BETWEEN :from AND :to")
    long countDeliveredBetween(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(sh) FROM SmsHistory sh WHERE sh.status = 'FAILED' AND sh.sentAt BETWEEN :from AND :to")
    long countFailedBetween(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, sh.sentAt, sh.deliveryAt)) FROM SmsHistory sh " +
            "WHERE sh.deliveryAt IS NOT NULL AND sh.sentAt BETWEEN :from AND :to")
    Double averageDeliveryLatency(@Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query(value = "SELECT sh.status_code AS statusCode, COALESCE(sh.error_message, 'UNKNOWN') AS errorMessage, " +
            "COUNT(*) AS occurrences, MAX(sh.updated_at) AS mostRecentOccurrence " +
            "FROM sms_history sh WHERE sh.sent_at BETWEEN :from AND :to AND sh.status = 'FAILED' " +
            "GROUP BY sh.status_code, COALESCE(sh.error_message, 'UNKNOWN') " +
            "ORDER BY occurrences DESC LIMIT :limit", nativeQuery = true)
    List<SmsErrorAggregation> aggregateErrors(@Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("limit") int limit);

    @Query(value = "SELECT p.id AS patientId, p.mr_number AS mrNumber, p.patient_name AS patientName, p.phone_number AS phoneNumber, " +
            "SUM(CASE WHEN sh.status = 'FAILED' THEN 1 ELSE 0 END) AS failedAttempts, " +
            "SUM(CASE WHEN (p.phone_number IS NULL OR p.phone_number = '') THEN 1 ELSE 0 END) AS missingPhoneCount, " +
            "MAX(sh.updated_at) AS lastFailure FROM patients p " +
            "LEFT JOIN sms_history sh ON sh.patient_id = p.id AND sh.sent_at BETWEEN :from AND :to " +
            "GROUP BY p.id, p.mr_number, p.patient_name, p.phone_number " +
            "HAVING failedAttempts >= :threshold OR missingPhoneCount > 0 " +
            "ORDER BY failedAttempts DESC, lastFailure DESC", nativeQuery = true)
    List<PatientNotificationIssueProjection> findPatientIssues(@Param("from") LocalDateTime from,
                                                               @Param("to") LocalDateTime to,
                                                               @Param("threshold") long threshold);
}
