package com.wooteco.wiki.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ApiResponse<B> extends ResponseEntity<B> {

    public ApiResponse(B body, HttpStatus status) {
        super(body, status);
    }

    @Getter
    @AllArgsConstructor
    public static class SuccessBody<D> {
        private D data;
        private String message;
        private int statusCode;
    }

    @Getter
    @AllArgsConstructor
    public static class FailureBody {
        private int statusCode;
        private String message;
    }
}
