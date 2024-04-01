package com.wooteco.wiki.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Slf4j
public class HttpLogger extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);
        ContentCachingRequestWrapper wrappingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappingResponse = new ContentCachingResponseWrapper(response);
        printRequestUriAndHeaders(wrappingRequest);
        filterChain.doFilter(wrappingRequest, wrappingResponse);
        wrappingResponse.addHeader("requestId", requestId);
        String responseBodyLog = makeResponseBodyLog(wrappingResponse);
        wrappingResponse.copyBodyToResponse();
        List<String> responseHeaderLogs = makeResponseHeaderLogs(requestId, response);
        printResponseLogs(requestId, responseHeaderLogs, responseBodyLog);
    }

    private void printRequestUriAndHeaders(ContentCachingRequestWrapper request) {
        String logType = "<<Request>>";
        String requestId = (String) request.getAttribute("requestId");
        log.info("{} = {} {} {} = {}&{}", "RequestId", requestId, logType, "URI", request.getRequestURI(),
                request.getQueryString());
        log.info("{} = {} {} {}", "RequestId", requestId, logType, "Headers");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String header = request.getHeader(headerName);
            log.info("{} = {} {} {} = {}", "RequestId", requestId, logType, headerName, header);
        }
    }

    private String makeResponseBodyLog(ContentCachingResponseWrapper responseWrapper) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(responseWrapper.getContentAsByteArray()).toPrettyString();
        } catch (IOException e) {
            return responseWrapper.getContentType() + " body json 아니라 파싱 불가";
        }
    }

    private List<String> makeResponseHeaderLogs(String requestId, HttpServletResponse response) {
        List<String> responseHeaderLogs = new ArrayList<>();
        responseHeaderLogs.add(String.format("%s = %s %s %s", "RequestId", requestId, "<<Response>>", "Headers"));
        for (String s : response.getHeaderNames()) {
            String headerLog = s + " = " + response.getHeader(s);
            responseHeaderLogs.add(headerLog);
        }
        return responseHeaderLogs;
    }

    private void printResponseLogs(String requestId, List<String> responseHeaderLogs, String responseBodyLog) {
        for (String s : responseHeaderLogs) {
            log.info(s);
        }
        log.info("{} = {} {} {} = \n{}", "RequestId", requestId, "<<Response>>", "Body", responseBodyLog);
    }
}
