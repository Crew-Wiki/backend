package com.wooteco.wiki.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class WrongEmailException extends WikiException {
    public WrongEmailException(String message) {
        super(message, BAD_REQUEST);
    }
}
