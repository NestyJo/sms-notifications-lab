package com.muhimbili.labnotification.service;

import com.muhimbili.labnotification.data.response.ExternalLabResultsResponse;
import com.muhimbili.labnotification.data.response.LabResultsPreviewResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class LabResultsTransformer {

    public LabResultsPreviewResponse transform(ExternalLabResultsResponse externalResponse) {
        if (externalResponse == null || externalResponse.getData() == null) {
            return LabResultsPreviewResponse.builder()
                    .message("Lab results preview")
                    .status(204)
                    .processingStatus("No Data")
                    .patients(List.of())
                    .build();
        }

        ExternalLabResultsResponse.DataPayload payload = externalResponse.getData();
        List<ExternalLabResultsResponse.LabResultItem> labResults = payload.getLabResults();
        Map<String, LabResultsPreviewResponse.PatientSummary> patientsMap = new LinkedHashMap<>();

        if (labResults != null) {
            for (ExternalLabResultsResponse.LabResultItem item : labResults) {
                String mrNumber = Objects.toString(item.getMrNumber(), "UNKNOWN");
                LabResultsPreviewResponse.PatientSummary patientSummary = patientsMap.computeIfAbsent(mrNumber, mr ->
                        LabResultsPreviewResponse.PatientSummary.builder()
                                .mrNumber(mr)
                                .patientName(item.getPatientName())
                                .phoneNumber(item.getPatMobile())
                                .orders(new ArrayList<>())
                                .build()
                );

                LabResultsPreviewResponse.OrderSummary orderSummary = resolveOrderSummary(patientSummary, item);
                TestGrouping grouping = groupingFrom(item);
                if (!StringUtils.hasText(grouping.profileCode)) {
                    orderSummary.getStandaloneTests().add(buildTestSummary(item));
                } else {
                    LabResultsPreviewResponse.ProfileSummary profileSummary = orderSummary.getProfiles().stream()
                            .filter(profile -> grouping.profileCode.equalsIgnoreCase(profile.getProfileCode()))
                            .findFirst()
                            .orElseGet(() -> {
                                LabResultsPreviewResponse.ProfileSummary newProfile = LabResultsPreviewResponse.ProfileSummary.builder()
                                        .profileCode(grouping.profileCode)
                                        .tests(new ArrayList<>())
                                        .build();
                                orderSummary.getProfiles().add(newProfile);
                                return newProfile;
                            });
                    profileSummary.getTests().add(buildTestSummary(item));
                }
            }
        }

        List<LabResultsPreviewResponse.PatientSummary> patientSummaries = new ArrayList<>(patientsMap.values());
        int totalOrders = patientSummaries.stream()
                .mapToInt(p -> p.getOrders().size())
                .sum();
        int totalTests = patientSummaries.stream()
                .flatMap(p -> p.getOrders().stream())
                .mapToInt(order -> order.getStandaloneTests().size() + order.getProfiles().stream()
                        .mapToInt(profile -> profile.getTests().size())
                        .sum())
                .sum();

        return LabResultsPreviewResponse.builder()
                .message("Lab results preview")
                .status(externalResponse.getStatus())
                .resultsDate(payload.getResultsDate())
                .fromTime(payload.getFromTime())
                .toTime(payload.getToTime())
                .processingStatus(payload.getProcessingStatus())
                .totalPatients(patientSummaries.size())
                .totalOrders(totalOrders)
                .totalTests(totalTests)
                .patients(patientSummaries)
                .build();
    }

    private LabResultsPreviewResponse.TestSummary buildTestSummary(ExternalLabResultsResponse.LabResultItem item) {
        return LabResultsPreviewResponse.TestSummary.builder()
                .testCode(item.getTestCode())
                .testName(item.getTestName())
                .resultStatus(item.getResultStatus())
                .orderStatus(item.getOrderStatus())
                .orderType(item.getOrderType())
                .labDepartment(buildLabDepartment(item))
                .build();
    }

    private LabResultsPreviewResponse.LabDepartmentSummary buildLabDepartment(ExternalLabResultsResponse.LabResultItem item) {
        return LabResultsPreviewResponse.LabDepartmentSummary.builder()
                .code(item.getLabDeptcode())
                .description(item.getLabdeptDesc())
                .labName(item.getLabDesc())
                .labType(item.getLabType())
                .labCode(item.getLabCode())
                .build();
    }

    private LabResultsPreviewResponse.OrderSummary resolveOrderSummary(LabResultsPreviewResponse.PatientSummary patientSummary,
                                                                       ExternalLabResultsResponse.LabResultItem item) {
        return patientSummary.getOrders().stream()
                .filter(order -> order.getOrderNum().equalsIgnoreCase(item.getOrderNum()))
                .findFirst()
                .orElseGet(() -> {
                    LabResultsPreviewResponse.OrderSummary orderSummary = LabResultsPreviewResponse.OrderSummary.builder()
                            .orderNum(item.getOrderNum())
                            .orderDate(item.getOrderDate())
                            .orderTime(item.getOrderTime())
                            .collectedAt(resolveCollectedAt(item))
                            .orderStatus(item.getOrderStatus())
                            .resultStatus(item.getResultStatus())
                            .orderType(item.getOrderType())
                            .profiles(new ArrayList<>())
                            .standaloneTests(new ArrayList<>())
                            .build();
                    patientSummary.getOrders().add(orderSummary);
                    return orderSummary;
                });
    }

    private String resolveCollectedAt(ExternalLabResultsResponse.LabResultItem item) {
        if (item.getOrderDate() == null || item.getOrderTime() == null) {
            return null;
        }
        return item.getOrderDate() + " " + item.getOrderTime();
    }

    private TestGrouping groupingFrom(ExternalLabResultsResponse.LabResultItem item) {
        return new TestGrouping(item.getProfileCode());
    }

    private record TestGrouping(String profileCode) {}
}
