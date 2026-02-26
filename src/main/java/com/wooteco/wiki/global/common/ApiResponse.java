package com.wooteco.wiki.global.common;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.SuccessCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse<B> extends ResponseEntity<B> {

    public ApiResponse(B body, HttpStatus status) {
        super(body, status);
    }

    public static record SuccessBody<D>(D data, SuccessCode code) {
    }

    public static record FailureBody(ErrorCode code, String message) {
    }
}
