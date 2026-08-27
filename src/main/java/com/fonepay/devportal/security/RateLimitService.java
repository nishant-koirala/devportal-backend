package com.fonepay.devportal.security;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.TooManyRequestsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    public static final int AUTH_MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MILLIS = 60_000L;

    private final Clock clock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void checkAuthEmail(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String key = "auth-email:" + email.trim().toLowerCase();
        long now = clock.millis();
        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartMillis >= WINDOW_SIZE_MILLIS) {
                return new Bucket(now, new AtomicInteger(1));
            }
            existing.counter.incrementAndGet();
            return existing;
        });

        if (bucket.counter.get() > AUTH_MAX_REQUESTS_PER_MINUTE) {
            long resetSeconds = Math.max(1, (WINDOW_SIZE_MILLIS - (now - bucket.windowStartMillis)) / 1000);
            log.warn("Auth rate limit exceeded for email {}", email.trim().toLowerCase());
            throw new TooManyRequestsException(
                    "Too many requests. Rate limit exceeded. Please retry in " + resetSeconds + " seconds.");
        }

        if (buckets.size() > 5000) {
            buckets.entrySet().removeIf(entry -> now - entry.getValue().windowStartMillis > WINDOW_SIZE_MILLIS * 2);
        }
    }

    private static class Bucket {
        final long windowStartMillis;
        final AtomicInteger counter;

        Bucket(long windowStartMillis, AtomicInteger counter) {
            this.windowStartMillis = windowStartMillis;
            this.counter = counter;
        }
    }
}
