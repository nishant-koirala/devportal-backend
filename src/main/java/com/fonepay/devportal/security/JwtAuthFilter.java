package com.fonepay.devportal.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fonepay.devportal.common.constant.AuthMessages;
import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.repository.UserSessionRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Value("${session.activity-debounce}")
    private Duration activityDebounce;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/") && !path.equals("/api/v1/auth/logout");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (jwtUtil.isWellFormedExpiredToken(token)) {
                writeUnauthorized(response, AuthMessages.SESSION_EXPIRED);
                return;
            }

            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.extractUserId(token);
                String sessionId = jwtUtil.extractSessionId(token);

                if (userId != null && sessionId != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserSession session = userSessionRepository.findBySessionId(sessionId).orElse(null);

                    if (session != null && session.getStatus() == SessionStatus.ACTIVE) {
                        Instant now = clock.instant();
                        if (isSessionExpired(session, now)) {
                            session.setStatus(SessionStatus.EXPIRED);
                            userSessionRepository.save(session);
                            log.warn("Session {} has expired for user {}", sessionId, userId);
                            writeUnauthorized(response, AuthMessages.SESSION_EXPIRED);
                            return;
                        }

                        touchActivityIfDue(session, now);

                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            List<SimpleGrantedAuthority> authorities = buildAuthorities(token);

                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities);
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to authenticate user via JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSessionExpired(UserSession session, Instant now) {
        if (session.getMaxExpiresAt() != null && !session.getMaxExpiresAt().isAfter(now)) {
            return true;
        }
        return session.getExpiresAt() != null && !session.getExpiresAt().isAfter(now);
    }

    private void touchActivityIfDue(UserSession session, Instant now) {
        Instant lastActivity = session.getLastActivityAt() != null
                ? session.getLastActivityAt()
                : session.getCreatedAt();
        if (lastActivity != null && now.isBefore(lastActivity.plus(activityDebounce))) {
            return;
        }

        Duration idleWindow = lastActivity != null && session.getExpiresAt() != null
                ? Duration.between(lastActivity, session.getExpiresAt())
                : Duration.ZERO;
        if (idleWindow.isNegative() || idleWindow.isZero()) {
            return;
        }

        session.setLastActivityAt(now);
        Instant slidExpiresAt = now.plus(idleWindow);
        if (session.getMaxExpiresAt() != null && slidExpiresAt.isAfter(session.getMaxExpiresAt())) {
            slidExpiresAt = session.getMaxExpiresAt();
        }
        session.setExpiresAt(slidExpiresAt);
        userSessionRepository.save(session);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now(clock))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }

    private List<SimpleGrantedAuthority> buildAuthorities(String token) {
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

        List<String> roles = jwtUtil.extractRoles(token);
        if (roles != null && !roles.isEmpty()) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }

        List<String> permissions = jwtUtil.extractPermissions(token);
        if (permissions != null && !permissions.isEmpty()) {
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
