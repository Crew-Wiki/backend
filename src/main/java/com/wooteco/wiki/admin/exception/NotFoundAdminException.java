package com.wooteco.wiki.admin.exception;

import com.wooteco.wiki.global.exception.WikiException;
import org.springframework.http.HttpStatus;

public class NotFoundAdminException extends WikiException {

    private static final String DEFAULT_MESSAGE = "해당 관리자를 찾을 수 없습니다";

    public NotFoundAdminException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundAdminException() {
        this(DEFAULT_MESSAGE);
    }
}
