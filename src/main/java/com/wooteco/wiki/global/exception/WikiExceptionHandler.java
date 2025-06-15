package com.wooteco.wiki.global.exception;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class WikiExceptionHandler {

    @ExceptionHandler(WikiException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(WikiException exception) {
        log.error(exception.getMessage(), exception);
        HttpStatus httpStatus = exception.getHttpStatus();
        return ApiResponseGenerator.fail(exception.getMessage(), httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<ApiResponse.FailureBody> handle(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ApiResponseGenerator.fail("알 수 없는 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
