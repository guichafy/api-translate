package sample_bedrock.translate.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates MDC with HTTP-centric data so Logback can render structured JSON logs consistently.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Map<String, String> context = new LinkedHashMap<>();
        context.put("requestId", resolveRequestId(request));
        context.put("traceId", resolveTraceId());
        context.put("spanId", generateSpanId());
        context.put("http.method", request.getMethod());
        context.put("http.path", resolveFullPath(request));

        String clientIp = resolveClientIp(request);
        if (StringUtils.hasText(clientIp)) {
            context.put("http.clientIp", clientIp);
        }

        String userAgent = request.getHeader("User-Agent");
        if (StringUtils.hasText(userAgent)) {
            context.put("http.userAgent", userAgent);
        }

        StatusCapturingHttpServletResponse responseWrapper = new StatusCapturingHttpServletResponse(response);
        context.put("http.status", String.valueOf(responseWrapper.currentStatus()));

        context.forEach((key, value) -> {
            if (StringUtils.hasText(value)) {
                MDC.put(key, value);
            }
        });

        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            MDC.put("http.status", String.valueOf(responseWrapper.currentStatus()));
            context.keySet().forEach(MDC::remove);
            MDC.remove("http.status");
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = firstNonBlank(request.getHeader(HEADER_REQUEST_ID), request.getHeader(HEADER_CORRELATION_ID));
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        if (!StringUtils.hasText(traceId)) {
            traceId = randomHex(32);
        }
        return traceId;
    }

    private String generateSpanId() {
        return randomHex(16);
    }

    private String resolveFullPath(HttpServletRequest request) {
        StringBuilder path = new StringBuilder(request.getRequestURI());
        if (StringUtils.hasText(request.getQueryString())) {
            path.append('?').append(request.getQueryString());
        }
        return path.toString();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex > -1 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate.trim();
            }
        }
        return null;
    }

    private String randomHex(int length) {
        String value = UUID.randomUUID().toString().replaceAll("-", "");
        return value.substring(0, Math.min(length, value.length()));
    }

    private static final class StatusCapturingHttpServletResponse extends HttpServletResponseWrapper {
        private int status = HttpServletResponse.SC_OK;

        private StatusCapturingHttpServletResponse(HttpServletResponse response) {
            super(response);
            updateStatus(HttpServletResponse.SC_OK);
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            updateStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            updateStatus(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            updateStatus(sc);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            updateStatus(HttpServletResponse.SC_FOUND);
        }

        private void updateStatus(int sc) {
            this.status = sc;
            MDC.put("http.status", String.valueOf(sc));
        }

        private int currentStatus() {
            return status;
        }
    }
}
