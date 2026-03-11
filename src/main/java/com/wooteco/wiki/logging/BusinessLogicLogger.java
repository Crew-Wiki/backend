package com.wooteco.wiki.logging;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@Order(1)
public class BusinessLogicLogger {

    private static final String NOT_AVAILABLE = "N/A";
    private static final String DOCUMENT_SERVICE_PACKAGE = "com.wooteco.wiki.document.service.";
    private static final String ORGANIZATION_DOCUMENT_SERVICE_PACKAGE = "com.wooteco.wiki.organizationdocument.service.";

    @Around("execution(* com.wooteco.wiki..service..*(..))")
    public Object traceCrud(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        if (!isDocumentService(className)) {
            return joinPoint.proceed();
        }

        CrudAction crudAction = resolveCrudAction(methodName);
        if (crudAction == CrudAction.NONE) {
            return joinPoint.proceed();
        }

        RequestInfo requestInfo = getRequestInfo();
        String arguments = summarizeArguments(joinPoint.getArgs());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - start;

            log.info(
                    "crud_trace status=SUCCESS action={} class={} method={} requestId={} httpMethod={} uri={} durationMs={} args={}",
                    crudAction,
                    className,
                    methodName,
                    requestInfo.requestId(),
                    requestInfo.httpMethod(),
                    requestInfo.uri(),
                    durationMs,
                    arguments
            );
            return result;
        } catch (Throwable throwable) {
            long durationMs = System.currentTimeMillis() - start;

            log.error(
                    "crud_trace status=FAILED action={} class={} method={} requestId={} httpMethod={} uri={} durationMs={} errorType={} errorMessage={} args={}",
                    crudAction,
                    className,
                    methodName,
                    requestInfo.requestId(),
                    requestInfo.httpMethod(),
                    requestInfo.uri(),
                    durationMs,
                    throwable.getClass().getSimpleName(),
                    sanitize(throwable.getMessage()),
                    arguments
            );
            throw throwable;
        }
    }

    private boolean isDocumentService(String className) {
        return className.startsWith(DOCUMENT_SERVICE_PACKAGE)
                || className.startsWith(ORGANIZATION_DOCUMENT_SERVICE_PACKAGE);
    }

    private CrudAction resolveCrudAction(String methodName) {
        String normalized = methodName.toLowerCase(Locale.ROOT);

        if (startsWithAny(normalized, "post", "create", "add", "link")) {
            return CrudAction.CREATE;
        }
        if (startsWithAny(normalized, "put", "update", "modify", "flush")) {
            return CrudAction.UPDATE;
        }
        if (startsWithAny(normalized, "delete", "remove", "unlink")) {
            return CrudAction.DELETE;
        }
        if (startsWithAny(normalized, "get", "find", "search", "read")) {
            return CrudAction.READ;
        }

        return CrudAction.NONE;
    }

    private boolean startsWithAny(String value, String... prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String summarizeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
        for (Object arg : args) {
            stringJoiner.add(summarizeArgument(arg));
        }
        return stringJoiner.toString();
    }

    private String summarizeArgument(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof UUID || arg instanceof Number || arg instanceof Boolean) {
            return arg.toString();
        }
        if (arg instanceof CharSequence text) {
            return "'" + truncate(text.toString(), 80) + "'";
        }
        if (arg instanceof Collection<?> collection) {
            return arg.getClass().getSimpleName() + "(size=" + collection.size() + ")";
        }
        if (arg instanceof Map<?, ?> map) {
            return arg.getClass().getSimpleName() + "(size=" + map.size() + ")";
        }
        if (arg.getClass().isArray()) {
            return arg.getClass().getSimpleName() + "(length=" + Array.getLength(arg) + ")";
        }

        String identifier = extractIdentifier(arg);
        if (identifier.isBlank()) {
            return arg.getClass().getSimpleName();
        }
        return arg.getClass().getSimpleName() + "(" + identifier + ")";
    }

    private String extractIdentifier(Object target) {
        StringJoiner joiner = new StringJoiner(", ");
        appendIdentifier(joiner, target, "getUuid", "uuid");
        appendIdentifier(joiner, target, "getId", "id");
        appendIdentifier(joiner, target, "getTitle", "title");
        appendIdentifier(joiner, target, "getWriter", "writer");
        return joiner.toString();
    }

    private void appendIdentifier(StringJoiner joiner, Object target, String getterName, String label) {
        try {
            Method method = target.getClass().getMethod(getterName);
            if (method.getParameterCount() != 0) {
                return;
            }
            Object value = method.invoke(target);
            if (value != null) {
                joiner.add(label + "=" + sanitize(value.toString()));
            }
        } catch (Exception ignored) {
        }
    }

    private RequestInfo getRequestInfo() {
        try {
            HttpServletRequest request = getRequest();
            String requestId = request.getAttribute("requestId") instanceof String id
                    ? id
                    : NOT_AVAILABLE;
            String queryString = request.getQueryString();
            String uri = request.getRequestURI();
            if (queryString != null && !queryString.isBlank()) {
                uri = uri + "?" + queryString;
            }
            return new RequestInfo(requestId, request.getMethod(), uri);
        } catch (IllegalStateException exception) {
            return new RequestInfo(NOT_AVAILABLE, NOT_AVAILABLE, NOT_AVAILABLE);
        }
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return NOT_AVAILABLE;
        }
        return truncate(value.replace('\n', ' ').replace('\r', ' '), 120);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private enum CrudAction {
        CREATE, READ, UPDATE, DELETE, NONE
    }

    private record RequestInfo(String requestId, String httpMethod, String uri) {
    }
}
