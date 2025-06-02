package com.wooteco.wiki.global.auth.exception;

import com.wooteco.wiki.global.exception.WikiException;
import org.springframework.http.HttpStatus;

public class TokenCreateException extends WikiException {

    private static final String DEFAULT_MESSAGE_FILED = "토큰 생성에 실패했습니다.";

    public TokenCreateException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public TokenCreateException() {
        this(DEFAULT_MESSAGE_FILED);
    }
}
