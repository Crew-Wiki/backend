package com.wooteco.wiki.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode {

    SUCCESS("요청이 성공하였습니다.", HttpStatus.OK);

    private final String message;
    private final HttpStatus httpStatus;

    SuccessCode(
            String message,
            HttpStatus httpStatus
    ) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
