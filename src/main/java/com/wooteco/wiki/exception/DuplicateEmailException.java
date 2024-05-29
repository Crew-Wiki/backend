package com.wooteco.wiki.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends WikiException {
    public DuplicateEmailException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
