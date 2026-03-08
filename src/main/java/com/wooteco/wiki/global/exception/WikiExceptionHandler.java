package com.wooteco.wiki.global.exception;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class WikiExceptionHandler {

    @ExceptionHandler(WikiException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(WikiException exception) {
        logError(
                exception,
                exception.getErrorCode().name(),
                exception.getMessage()
        );
        return ApiResponseGenerator.failure(exception.getErrorCode(), exception.getMessage(),
                exception.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<ApiResponse.FailureBody> handle(Exception exception) {
        logError(
                exception,
                ErrorCode.UNKNOWN_ERROR.name(),
                "An unknown error occurred."
        );
        return ApiResponseGenerator.failure(ErrorCode.UNKNOWN_ERROR, "An unknown error occurred.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<ApiResponse.FailureBody> handle(MethodArgumentNotValidException exception) {
        logError(
                exception,
                ErrorCode.VALIDATION_ERROR.name(),
                exception.getMessage()
        );
        return ApiResponseGenerator.failure(ErrorCode.VALIDATION_ERROR);
    }

    private void logError(Exception exception, String errorCode, String message) {
        RequestInfo requestInfo = getRequestInfo();
        log.error(
                "api_exception requestId={} httpMethod={} uri={} errorCode={} message={}",
                requestInfo.requestId,
                requestInfo.httpMethod,
                requestInfo.uri,
                errorCode,
                message,
                exception
        );
    }

    private RequestInfo getRequestInfo() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            String requestId = request.getAttribute("requestId") instanceof String id ? id : "N/A";
            return new RequestInfo(requestId, request.getMethod(), request.getRequestURI());
        } catch (IllegalStateException e) {
            return new RequestInfo("N/A", "N/A", "N/A");
        }
    }

    private record RequestInfo(String requestId, String httpMethod, String uri) {
    }
}
