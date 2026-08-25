package com.fonepay.devportal.modules.cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fonepay.devportal.security.filter.PublicRateLimitingFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;


@ExtendWith(MockitoExtension.class)
class PublicRateLimitingFilterTest {

    @Mock
    private FilterChain filterChain;

    private Clock clock;
    private ObjectMapper objectMapper;
    private PublicRateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-08-25T10:00:00Z"), ZoneId.of("UTC"));
        filter = new PublicRateLimitingFilter(clock);
    }



    @Test
    @DisplayName("Public requests within limit proceed normally with rate limit headers")
    void doFilter_withinLimit_proceeds() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/products");
        request.setServletPath("/api/v1/public/products");
        request.setRemoteAddr("198.51.100.22");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("60", response.getHeader("X-RateLimit-Limit"));
        assertEquals("59", response.getHeader("X-RateLimit-Remaining"));
        assertNotNull(response.getHeader("X-RateLimit-Reset"));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Public requests exceeding 60 requests per minute receive 429 Too Many Requests")
    void doFilter_exceedingLimit_returns429() throws ServletException, IOException {
        String clientIp = "198.51.100.99";

        // Perform 60 requests (allowed)
        for (int i = 0; i < 60; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public/products");
            request.setServletPath("/api/v1/public/products");
            request.setRemoteAddr(clientIp);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        // 61st request should be blocked with 429
        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("GET", "/api/v1/public/products");
        blockedRequest.setServletPath("/api/v1/public/products");
        blockedRequest.setRemoteAddr(clientIp);
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        filter.doFilter(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
        assertEquals("60", blockedResponse.getHeader("X-RateLimit-Limit"));
        assertEquals("0", blockedResponse.getHeader("X-RateLimit-Remaining"));
        assertNotNull(blockedResponse.getHeader("Retry-After"));

        // Chain should only have been called 60 times, NOT 61 times
        verify(filterChain, times(60)).doFilter(any(), any());
    }
}
