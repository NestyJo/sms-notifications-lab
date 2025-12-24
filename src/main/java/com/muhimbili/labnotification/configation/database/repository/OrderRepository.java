package com.muhimbili.labnotification.configation.database.repository;

import com.muhimbili.labnotification.configation.database.entities.Order;
import com.muhimbili.labnotification.configation.database.projectors.OrderBacklogProjection;
import com.muhimbili.labnotification.configation.database.projectors.OrderDailyAggregation;
import com.muhimbili.labnotification.configation.database.projectors.OrderProjector;
import com.muhimbili.labnotification.configation.database.projectors.SmsOrderProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    OrderProjector findProjectedByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "patient")
    @Query("SELECT o FROM Order o WHERE o.resultStatus IN (:resultStatuses) AND o.statusCode = :statusCode ORDER BY o.updatedAt ASC")
    List<SmsOrderProjection> findSmsCandidates(@Param("resultStatuses") List<String> resultStatuses,
                                               @Param("statusCode") Integer statusCode,
                                               Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status, o.statusCode = :statusCode WHERE o.id IN :ids")
    int updateStatusForIds(@Param("status") Order.ProcessingStatus status,
                           @Param("statusCode") Integer statusCode,
                           @Param("ids") List<Long> ids);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countOrdersCreatedBetween(@Param("from") Instant from,
                                   @Param("to") Instant to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.muhimbili.labnotification.configation.database.entities.Order$ProcessingStatus.DONE " +
            "AND o.updatedAt BETWEEN :from AND :to")
    long countOrdersCompletedBetween(@Param("from") Instant from,
                                     @Param("to") Instant to);

    @Query("SELECT o.orderDate as orderDate, COUNT(o) as count FROM Order o " +
            "WHERE o.orderDate BETWEEN :from AND :to GROUP BY o.orderDate ORDER BY o.orderDate")
    List<OrderDailyAggregation> countDailyOrders(@Param("from") LocalDate from,
                                                 @Param("to") LocalDate to);

    @Query("SELECT o.orderDate as orderDate, COUNT(o) as count FROM Order o " +
            "WHERE o.status = com.muhimbili.labnotification.configation.database.entities.Order$ProcessingStatus.DONE " +
            "AND o.orderDate BETWEEN :from AND :to GROUP BY o.orderDate ORDER BY o.orderDate")
    List<OrderDailyAggregation> countDailyCompletedOrders(@Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatuses(@Param("statuses") List<Order.ProcessingStatus> statuses);

    @Query("SELECT o.id as id, o.orderNumber as orderNumber, o.orderStatus as orderStatus, o.resultStatus as resultStatus, " +
            "o.orderType as orderType, o.patient.mrNumber as patientMrNumber, o.patient.patientName as patientName, " +
            "o.collectedAt as collectedAt, o.createdAt as createdAt FROM Order o " +
            "WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    List<OrderBacklogProjection> findBacklogOrders(@Param("statuses") List<Order.ProcessingStatus> statuses, Pageable pageable);
}
