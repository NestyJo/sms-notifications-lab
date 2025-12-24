package com.muhimbili.labnotification.data.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalLabResultsResponse {

    private int status;
    private DataPayload data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPayload {
        private String resultsDate;
        private String fromTime;
        private String toTime;

        @SerializedName("status")
        private String processingStatus;

        @SerializedName("labresults")
        private List<LabResultItem> labResults;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabResultItem {

        @SerializedName("labDeptcode")
        private String labDeptcode;

        @SerializedName("labdeptDesc")
        private String labdeptDesc;

        @SerializedName("labDesc")
        private String labDesc;

        @SerializedName("labType")
        private String labType;

        @SerializedName("labCode")
        private String labCode;

        @SerializedName("labTypeName")
        private String labTypeName;

        @SerializedName("labdeptName")
        private String labdeptName;

        @SerializedName("labDepartment")
        private String labDepartment;

        @SerializedName("labTypeDesc")
        private String labTypeDesc;

        @SerializedName("labdeptCode")
        private String labdeptCodeDuplicate;

        @SerializedName("labdeptNameDesc")
        private String labdeptNameDesc;

        @SerializedName("labTypeCode")
        private String labTypeCode;

        @SerializedName("labDeptDesc")
        private String labDeptDescDuplicate;

        @SerializedName("labDescName")
        private String labDescName;

        @SerializedName("labName")
        private String labName;

        @SerializedName("labCodeDesc")
        private String labCodeDesc;

        @SerializedName("labCodeName")
        private String labCodeName;

        @SerializedName("labDescDesc")
        private String labDescDesc;

        @SerializedName("orderNum")
        private String orderNum;

        @SerializedName("testCode")
        private String testCode;

        @SerializedName("testName")
        private String testName;

        @SerializedName("testProfile")
        private String testProfile;

        @SerializedName("profileCode")
        private String profileCode;

        @SerializedName("orderDate")
        private String orderDate;

        @SerializedName("orderTime")
        private String orderTime;

        @SerializedName("orderType")
        private String orderType;

        @SerializedName("mrNumber")
        private String mrNumber;

        @SerializedName("patientName")
        private String patientName;

        @SerializedName("orderStatus")
        private String orderStatus;

        @SerializedName("resultStatus")
        private String resultStatus;

        @SerializedName("patMobile")
        private String patMobile;
    }
}
