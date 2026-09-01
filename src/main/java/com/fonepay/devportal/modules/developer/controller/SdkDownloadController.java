package com.fonepay.devportal.modules.developer.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.developer.service.SdkDownloadService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.security.constant.SecurityExpressions;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Developer.BASE + "/sdks")
@PreAuthorize(SecurityExpressions.HAS_AUTHENTICATED)
@RequiredArgsConstructor
public class SdkDownloadController {

    private final SdkDownloadService sdkDownloadService;

    @GetMapping("/{language}")
    public ResponseEntity<Void> downloadSdk(
            @PathVariable @NotBlank String language,
            Authentication authentication) {

        User user = extractUser(authentication);
        log.info("Authenticated user [{}] requesting SDK download for language: [{}]", user.getUserId(), language);

        String downloadUrl = sdkDownloadService.resolveDownloadUrl(language);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(downloadUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private User extractUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
        }
        throw new UnauthorizedException("User session is invalid or unauthenticated");
    }
}
