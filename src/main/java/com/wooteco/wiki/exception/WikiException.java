package com.wooteco.wiki.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WikiException extends RuntimeException {
    private final HttpStatus httpStatus;

    public WikiException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    WikiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
