package com.muhimbili.labnotification.data.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LabResultsPreviewResponse {
    private String message;
    private int status;
    private String resultsDate;
    private String fromTime;
    private String toTime;
    private String processingStatus;
    private int totalPatients;
    private int totalOrders;
    private int totalTests;
    private List<PatientSummary> patients;

    @Data
    @Builder
    public static class PatientSummary {
        private String mrNumber;
        private String patientName;
        private String phoneNumber;
        private List<OrderSummary> orders;
    }

    @Data
    @Builder
    public static class OrderSummary {
        private String orderNum;
        private String orderDate;
        private String orderTime;
        private String collectedAt;
        private String orderStatus;
        private String resultStatus;
        private String orderType;
        private List<ProfileSummary> profiles;
        private List<TestSummary> standaloneTests;
    }

    @Data
    @Builder
    public static class ProfileSummary {
        private String profileCode;
        private List<TestSummary> tests;
    }

    @Data
    @Builder
    public static class TestSummary {
        private String testCode;
        private String testName;
        private String resultStatus;
        private String orderStatus;
        private String orderType;
        private LabDepartmentSummary labDepartment;
    }

    @Data
    @Builder
    public static class LabDepartmentSummary {
        private String code;
        private String description;
        private String labName;
        private String labType;
        private String labCode;
    }
}
