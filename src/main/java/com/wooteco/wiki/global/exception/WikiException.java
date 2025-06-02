package com.wooteco.wiki.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WikiException extends RuntimeException {
    private final HttpStatus httpStatus;

    public WikiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
