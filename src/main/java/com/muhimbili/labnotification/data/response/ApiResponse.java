package com.muhimbili.labnotification.data.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiResponse<T> {
    int statusCode;
    String message;
    T data;
}
