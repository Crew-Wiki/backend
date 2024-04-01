package com.wooteco.wiki.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@Aspect
@Component
@Order(1)
public class BusinessLogicLogger {

    @Around("execution(* com.wooteco.wiki.controller.*Controller.*(..)) "
            + "|| execution(* com.wooteco.wiki.service.*Service.*(..)) "
            + "|| execution(* com.wooteco.wiki.domain.*.*(..)) "
            + "|| execution(* com.wooteco.wiki.repository.*Repository.*(..)) ")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String type = getBusinessType(className);
        if (className.contains("Controller")) {
            printRequestBody();
        }
        try {
            HttpServletRequest request = getRequest();
            String requestId = (String) request.getAttribute("requestId");
            log.info("{} = {} {} {}.{}()", "RequestId", requestId, type, className, methodName);
        } catch (IllegalStateException e) {
            log.info("{} = {} {} {}.{}()", "Scheduler", className, type, className, methodName);
        }
        return joinPoint.proceed();
    }

    private String getBusinessType(String className) {
        if (className.contains("Controller")) {
            return "<<Controller>>";
        }
        if (className.contains("Service")) {
            return "<<Service>>";
        }
        if (className.contains("Repository")) {
            return "<<Repository>>";
        }
        if (className.contains("Scheduler")) {
            return "<<Scheduler>>";
        }
        return "";
    }

    private void printRequestBody() {
        HttpServletRequest request = getRequest();
        final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;
        String requestId = (String) cachingRequest.getAttribute("requestId");
        String logType = "<<Request>>";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.info("{} = {} {} {} = \n{}", "RequestId", requestId, logType, "Body",
                    objectMapper.readTree(cachingRequest.getContentAsByteArray()).toPrettyString());
        } catch (Exception e) {
            log.info("{} = {} {} {} = \n{}", "RequestId", requestId, logType, "Body", " ");
        }
    }

    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
