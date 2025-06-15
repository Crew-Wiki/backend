package com.wooteco.wiki.global.common;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass
public class ApiResponseGenerator {

    public static ApiResponse<ApiResponse.SuccessBody<Void>> success(HttpStatus status, String message) {
        return new ApiResponse<>(new ApiResponse.SuccessBody<>(null, message, status.value()), status);
    }

    public static <D> ApiResponse<ApiResponse.SuccessBody<D>> success(D data, HttpStatus status, String message) {
        return new ApiResponse<>(new ApiResponse.SuccessBody<>(data, message, status.value()), status);
    }

    public static ApiResponse<ApiResponse.FailureBody> fail(String message, HttpStatus status) {
        return new ApiResponse<>(new ApiResponse.FailureBody(status.value(), message), status);
    }
}
