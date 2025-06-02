package com.wooteco.wiki.document.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

import com.wooteco.wiki.global.exception.WikiException;

public class DuplicateDocumentException extends WikiException {
    public DuplicateDocumentException(String message) {
        super(message, CONFLICT);
    }
}
