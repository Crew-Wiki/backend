package com.wooteco.wiki.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    PAGE_BAD_REQUEST("페이징 요청이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("검증에 실패하였습니다.", HttpStatus.BAD_REQUEST),

    TOKEN_CREATE_ERROR("토큰 생성에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    WRONG_TOKEN("잘못된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),

    FORBIDDEN("권한이 없습니다.", HttpStatus.FORBIDDEN),

    DOCUMENT_NOT_FOUND("문서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ADMIN_NOT_FOUND("관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    VERSION_NOT_FOUND("버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ORGANIZATION_DOCUMENT_NOT_FOUND("조직 문서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ORGANIZATION_EVENT_NOT_FOUND("조직 이벤트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    DOCUMENT_DUPLICATE("이미 존재하는 문서입니다.", HttpStatus.CONFLICT),

    UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(
            String message,
            HttpStatus httpStatus
    ) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
} 
