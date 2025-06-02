package com.wooteco.wiki.document.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.wooteco.wiki.global.exception.WikiException;

public class DocumentNotFoundException extends WikiException {
    public DocumentNotFoundException(String message) {
        super(message, NOT_FOUND);
    }
}
