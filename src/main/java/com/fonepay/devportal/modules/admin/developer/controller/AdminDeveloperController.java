package com.fonepay.devportal.modules.admin.developer.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.admin.developer.dto.request.DeveloperSearchCriteriaDto;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperResponseDto;
import com.fonepay.devportal.modules.admin.developer.service.DeveloperManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.BASE + ApiRoutes.Admin.DEVELOPERS)
@RequiredArgsConstructor
public class AdminDeveloperController {

    private final DeveloperManagementService developerManagementService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeveloperResponseDto>>> getDevelopers(
            @Valid @ModelAttribute DeveloperSearchCriteriaDto criteria) {

        PageResponse<DeveloperResponseDto> pageResponse = developerManagementService.getDevelopers(criteria);

        ApiResponse<PageResponse<DeveloperResponseDto>> response = ApiResponse
                .<PageResponse<DeveloperResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Developers retrieved successfully")
                .data(pageResponse)
                .timestamp(LocalDateTime.now(clock))
                .build();

        return ResponseEntity.ok(response);
    }
}
