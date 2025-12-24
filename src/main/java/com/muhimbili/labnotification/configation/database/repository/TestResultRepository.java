package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.TestResult;
import com.muhimbili.labnotification.configation.database.projectors.TestDepartmentAggregation;
import com.muhimbili.labnotification.configation.database.projectors.TestResultProjector;
import com.muhimbili.labnotification.configation.database.projectors.TestTatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findByOrderIdAndTestCode(Long orderId, String testCode);

    TestResultProjector findProjectedById(Long id);

    @Query("SELECT t FROM TestResult t WHERE t.order.id IN :orderIds")
    List<TestResult> findAllByOrderIds(@Param("orderIds") Collection<Long> orderIds);

    @Query("SELECT COUNT(t) FROM TestResult t WHERE t.resultStatus IN ('FINAL', 'DONE') AND t.updatedAt BETWEEN :from AND :to")
    long countCompletedBetween(@Param("from") Instant from,
                               @Param("to") Instant to);

    @Query("SELECT t.labDepartment.id as departmentId, COALESCE(t.labDepartment.description, t.labDepartment.code) as departmentName, " +
            "COUNT(t) as totalTests, " +
            "SUM(CASE WHEN t.resultStatus IN ('PENDING', 'PROCESSING') THEN 1 ELSE 0 END) as pendingResults, " +
            "SUM(CASE WHEN t.resultStatus IN ('FINAL', 'DONE') THEN 1 ELSE 0 END) as finalizedResults, " +
            "SUM(CASE WHEN t.resultStatus = 'ABNORMAL' THEN 1 ELSE 0 END) as abnormalResults " +
            "FROM TestResult t WHERE t.createdAt BETWEEN :from AND :to " +
            "AND (:departmentId IS NULL OR t.labDepartment.id = :departmentId) " +
            "GROUP BY t.labDepartment.id, t.labDepartment.description, t.labDepartment.code")
    List<TestDepartmentAggregation> aggregateByDepartment(@Param("from") Instant from,
                                                          @Param("to") Instant to,
                                                          @Param("departmentId") Long departmentId);

    @Query("SELECT t.id as testId, t.labDepartment.id as departmentId, COALESCE(t.labDepartment.description, t.labDepartment.code) as departmentName, " +
            "t.testCode as testCode, t.testName as testName, t.order.orderDate as orderDate, o.collectedAt as collectedAt, t.updatedAt as resultUpdatedAt " +
            "FROM TestResult t JOIN t.order o " +
            "WHERE o.collectedAt IS NOT NULL AND t.updatedAt IS NOT NULL " +
            "AND o.orderDate BETWEEN :from AND :to " +
            "AND (:departmentId IS NULL OR t.labDepartment.id = :departmentId)")
    List<TestTatProjection> findTatCandidates(@Param("from") LocalDate from,
                                             @Param("to") LocalDate to,
                                             @Param("departmentId") Long departmentId);
}
