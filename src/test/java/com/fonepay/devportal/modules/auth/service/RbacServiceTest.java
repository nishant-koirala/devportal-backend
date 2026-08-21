package com.fonepay.devportal.modules.auth.service;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.service.serviceImpl.RbacServiceImpl;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.service.UserRoleService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private RbacServiceImpl rbacService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasRole_ReturnsTrue_WhenUserHasRole() {
        when(userRoleService.getRoleNamesByUserId("user-1")).thenReturn(List.of("ADMIN", "DEVELOPER"));

        assertTrue(rbacService.hasRole("user-1", "ADMIN"));
        assertTrue(rbacService.hasRole("user-1", "admin"));
        assertFalse(rbacService.hasRole("user-1", "EDITOR"));
    }

    @Test
    void checkRole_ThrowsForbidden_WhenUserLacksRole() {
        when(userRoleService.getRoleNamesByUserId("user-1")).thenReturn(List.of("DEVELOPER"));

        assertThrows(ForbiddenException.class, () -> rbacService.checkRole("user-1", "ADMIN"));
    }

    @Test
    void validateCurrentUserRole_Success_WhenAuthenticatedWithRequiredRole() {
        User user = new User();
        user.setUserId("admin-1");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> rbacService.validateCurrentUserRole("ADMIN"));
        assertDoesNotThrow(() -> rbacService.validateCurrentUserRole("ADMIN", "EDITOR"));
    }

    @Test
    void validateCurrentUserRole_ThrowsForbidden_WhenAuthenticatedWithoutRequiredRole() {
        User user = new User();
        user.setUserId("user-dev");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(ForbiddenException.class, () -> rbacService.validateCurrentUserRole("ADMIN"));
    }

    @Test
    void validateCurrentUserRole_ThrowsUnauthorized_WhenUnauthenticated() {
        assertThrows(UnauthorizedException.class, () -> rbacService.validateCurrentUserRole("ADMIN"));
    }
}
