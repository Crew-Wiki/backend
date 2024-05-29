package com.wooteco.wiki.exception;

import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends WikiException {
    public MemberNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
