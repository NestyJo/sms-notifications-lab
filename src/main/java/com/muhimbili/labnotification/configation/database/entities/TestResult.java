package com.muhimbili.labnotification.configation.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "tests",
        uniqueConstraints = @UniqueConstraint(name = "uq_tests_order_test",
                columnNames = {"order_id", "test_code"}),
        indexes = {
                @Index(name = "idx_tests_profile", columnList = "profile_id"),
                @Index(name = "idx_tests_lab_department", columnList = "lab_department_id")
        })
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "lab_department_id")
    private LabDepartment labDepartment;

    @Column(name = "test_code", nullable = false, length = 50)
    private String testCode;

    @Column(name = "test_name", nullable = false, length = 255)
    private String testName;

    @Column(name = "result_status", nullable = false, length = 10)
    private String resultStatus;

    @Column(name = "order_status", nullable = false, length = 10)
    private String orderStatus;

    @Column(name = "order_type", nullable = false, length = 10)
    private String orderType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "tatTime", length = 255)
    private String tatTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public LabDepartment getLabDepartment() {
        return labDepartment;
    }

    public void setLabDepartment(LabDepartment labDepartment) {
        this.labDepartment = labDepartment;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTatTime() {
        return tatTime;
    }

    public void setTatTime(String tatTime) {
        this.tatTime = tatTime;
    }
}
