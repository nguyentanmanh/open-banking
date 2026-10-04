package com.manh.openbanking.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "Request-ID";
    public static final String ATTRIBUTE = RequestIdFilter.class.getName() + ".requestId";
    private static final Logger LOG = LoggerFactory.getLogger(RequestIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();
        request.setAttribute(ATTRIBUTE, requestId);
        response.setHeader(HEADER, requestId);
        long started = System.nanoTime();
        try {
            MDC.put("requestId", requestId);
            LOG.info("INBOUND | Request-ID={} | {} {} | operation={}", requestId, request.getMethod(),
                request.getRequestURI(), operation(request));
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            LOG.info("RESPONSE | Request-ID={} | status={} | elapsedMs={}", requestId, response.getStatus(), elapsedMs);
            MDC.remove("requestId");
        }
    }

    private String operation(HttpServletRequest request) {
        return switch (request.getRequestURI()) {
            case "/v1/accounts/transactions" -> "transaction-history";
            case "/internal/simulator/balance-changes" -> "balance-change-simulator";
            default -> "http-request";
        };
    }
}
