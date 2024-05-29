package com.wooteco.wiki.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class WrongPasswordException extends WikiException {
    public WrongPasswordException(String message) {
        super(message, BAD_REQUEST);
    }
}
