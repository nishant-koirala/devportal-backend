package com.fonepay.devportal.security;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.modules.user.entity.User;
import com.fonepay.devportal.modules.user.entity.UserSession;
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
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.extractUserId(token);
                String sessionId = jwtUtil.extractSessionId(token);

                if (userId != null && sessionId != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserSession session = userSessionRepository.findBySessionId(sessionId).orElse(null);

                    if (session != null && session.getStatus() == SessionStatus.ACTIVE) {
                        Instant now = clock.instant();
                        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(now)) {
                            session.setStatus(SessionStatus.EXPIRED);
                            userSessionRepository.save(session);
                            log.warn("Session {} has expired for user {}", sessionId, userId);
                        } else {
                            session.setLastActivityAt(now);
                            userSessionRepository.save(session);

                            User user = userRepository.findById(userId).orElse(null);
                            if (user != null) {
                                // Build authorities from the JWT token
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
            }
        } catch (Exception e) {
            log.error("Failed to authenticate user via JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(String token) {
        List<String> roles = jwtUtil.extractRoles(token);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());
    }
}
