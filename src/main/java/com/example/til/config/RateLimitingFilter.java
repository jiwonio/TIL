package com.example.til.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingFilter implements Filter {

    private final int limitPerMinute;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(int limitPerMinute) {
        this.limitPerMinute = Math.max(10, limitPerMinute); // enforce minimal sane limit
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse res)) {
            chain.doFilter(request, response);
            return;
        }
        String ip = getClientIp(req);
        long currentMinute = System.currentTimeMillis() / 60000L;
        String key = ip + ":" + currentMinute;
        Counter counter = counters.computeIfAbsent(key, k -> new Counter());
        int count = counter.incrementAndGet();
        if (count > limitPerMinute) {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setContentType("text/plain;charset=UTF-8");
            res.getWriter().write("Too Many Requests. Please try again later.");
            return;
        }
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Counter {
        private int value = 0;
        synchronized int incrementAndGet() { return ++value; }
    }
}
