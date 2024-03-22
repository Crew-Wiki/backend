package com.wooteco.wiki.exception;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.wooteco.wiki.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WikiExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handle(RuntimeException exception) {
        if (exception.getMessage().equals("제목이 겹치는 문서가 있습니다.")) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ErrorResponse("제목이 겹치는 문서가 있습니다."));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("알 수 없는 에러가 발생했습니다."));
    }
}
