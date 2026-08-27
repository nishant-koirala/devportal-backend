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

    private static final int PUBLIC_MAX_REQUESTS_PER_MINUTE = 60;
    private static final int AUTH_MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MILLIS = 60_000L;

    private static final String PUBLIC_PREFIX = "/api/v1/public/";
    private static final String AUTH_PREFIX = "/api/v1/auth/";

    private final Clock clock;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = resolvePath(request);
        return path == null || !(isPublicPath(path) || isAuthPath(path));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = resolvePath(request);
        String clientIp = HttpRequestUtil.getClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = request.getRemoteAddr();
        }

        int maxRequests = maxRequestsFor(path);
        String bucketKey = (isAuthPath(path) ? "auth:" : "public:") + clientIp;

        long now = clock.millis();
        RateLimitBucket bucket = buckets.compute(bucketKey, (key, existing) -> {
            if (existing == null || now - existing.windowStartMillis >= WINDOW_SIZE_MILLIS) {
                return new RateLimitBucket(now, new AtomicInteger(1));
            }
            existing.counter.incrementAndGet();
            return existing;
        });

        int currentCount = bucket.counter.get();
        long windowElapsed = now - bucket.windowStartMillis;
        long resetSeconds = Math.max(1, (WINDOW_SIZE_MILLIS - windowElapsed) / 1000);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - currentCount)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetSeconds));

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for IP {} on {} {}: {} requests in current window",
                    clientIp, isAuthPath(path) ? "auth" : "public", path, currentCount);

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

        if (buckets.size() > 5000) {
            cleanupStaleBuckets(now);
        }

        filterChain.doFilter(request, response);
    }

    private static String resolvePath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        return path;
    }

    private static boolean isPublicPath(String path) {
        return path.startsWith(PUBLIC_PREFIX);
    }

    private static boolean isAuthPath(String path) {
        return path.startsWith(AUTH_PREFIX);
    }

    private static int maxRequestsFor(String path) {
        return isAuthPath(path) ? AUTH_MAX_REQUESTS_PER_MINUTE : PUBLIC_MAX_REQUESTS_PER_MINUTE;
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
