package com.wooteco.wiki.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class WikiExceptionHandler {

    @ExceptionHandler(WikiException.class)
    public ResponseEntity<ErrorResponse> handle(WikiException exception) {
        log.error(exception.getMessage(), exception);
        HttpStatus httpStatus = exception.getHttpStatus();
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("알 수 없는 에러가 발생했습니다."));
    }
}
