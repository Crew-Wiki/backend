package com.wooteco.wiki.global.exception;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class WikiExceptionHandler {

    @ExceptionHandler(WikiException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(WikiException exception) {
        log.error(exception.getMessage(), exception);
        return ApiResponseGenerator.failure(exception.getErrorCode(), exception.getMessage(),
                exception.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<ApiResponse.FailureBody> handle(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ApiResponseGenerator.failure(ErrorCode.UNKNOWN_ERROR, "An unknown error occurred.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);
        return ApiResponseGenerator.failure(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(MissingServletRequestParameterException exception) {
        return handleRequestParameter(exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(MethodArgumentTypeMismatchException exception) {
        return handleRequestParameter(exception);
    }

    private ApiResponse<ApiResponse.FailureBody> handleRequestParameter(Exception exception) {
        log.warn(exception.getMessage());
        return ApiResponseGenerator.failure(ErrorCode.VALIDATION_ERROR);
    }
}
