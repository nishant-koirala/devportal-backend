package com.fonepay.devportal.security.filter;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fonepay.devportal.common.util.HttpRequestUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class PublicRateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long WINDOW_SIZE_MILLIS = 60_000L; // 1 minute

    private final Clock clock;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path == null || !path.startsWith("/api/v1/public/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = HttpRequestUtil.getClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = request.getRemoteAddr();
        }

        long now = clock.millis();
        RateLimitBucket bucket = buckets.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStartMillis >= WINDOW_SIZE_MILLIS) {
                return new RateLimitBucket(now, new AtomicInteger(1));
            }
            existing.counter.incrementAndGet();
            return existing;
        });

        int currentCount = bucket.counter.get();
        long windowElapsed = now - bucket.windowStartMillis;
        long resetSeconds = Math.max(1, (WINDOW_SIZE_MILLIS - windowElapsed) / 1000);

        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - currentCount)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetSeconds));

        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP {} on public endpoint {}: {} requests in current window",
                    clientIp, request.getServletPath(), currentCount);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(resetSeconds));

            String jsonResponse = String.format(
                    "{\"status\":429,\"success\":false,\"message\":\"Too many requests. Rate limit exceeded. Please retry in %d seconds.\",\"timestamp\":\"%s\"}",
                    resetSeconds,
                    LocalDateTime.now(clock).toString()
            );

            response.getWriter().write(jsonResponse);
            return;
        }


        // Periodically purge stale buckets to prevent memory leak
        if (buckets.size() > 5000) {
            cleanupStaleBuckets(now);
        }

        filterChain.doFilter(request, response);
    }

    private void cleanupStaleBuckets(long now) {
        buckets.entrySet().removeIf(entry -> now - entry.getValue().windowStartMillis > WINDOW_SIZE_MILLIS * 2);
    }

    private static class RateLimitBucket {
        final long windowStartMillis;
        final AtomicInteger counter;

        RateLimitBucket(long windowStartMillis, AtomicInteger counter) {
            this.windowStartMillis = windowStartMillis;
            this.counter = counter;
        }
    }
}
