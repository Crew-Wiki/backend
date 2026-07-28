package com.wooteco.wiki.global.common;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.SuccessCode;
import org.springframework.http.HttpStatus;

public class ApiResponseGenerator {

    public static <D> ApiResponse<ApiResponse.SuccessBody<D>> success(D data) {
        return new ApiResponse<>(new ApiResponse.SuccessBody<>(data, SuccessCode.SUCCESS), HttpStatus.OK);
    }

    public static <D> ApiResponse<ApiResponse.SuccessBody<D>> success(
            D data,
            HttpStatus status
    ) {
        return new ApiResponse<>(new ApiResponse.SuccessBody<>(data, SuccessCode.SUCCESS), status);
    }

    public static ApiResponse<ApiResponse.SuccessBody<Void>> success(HttpStatus status) {
        return new ApiResponse<>(new ApiResponse.SuccessBody<>(null, SuccessCode.SUCCESS), status);
    }

    public static ApiResponse<ApiResponse.FailureBody> failure(
            ErrorCode code,
            String message,
            HttpStatus status
    ) {
        return new ApiResponse<>(new ApiResponse.FailureBody(code, message), status);
    }

    public static ApiResponse<ApiResponse.FailureBody> failure(ErrorCode code) {
        return new ApiResponse<>(new ApiResponse.FailureBody(code, code.getMessage()), code.getHttpStatus());
    }
}
