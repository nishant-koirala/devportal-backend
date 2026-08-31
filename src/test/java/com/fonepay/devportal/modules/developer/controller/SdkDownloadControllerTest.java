package com.fonepay.devportal.modules.developer.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.GlobalExceptionHandler;
import com.fonepay.devportal.modules.developer.service.SdkDownloadService;
import com.fonepay.devportal.modules.user.document.User;

@ExtendWith(MockitoExtension.class)
class SdkDownloadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SdkDownloadService sdkDownloadService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneId.of("UTC"));

    @InjectMocks
    private SdkDownloadController sdkDownloadController;

    private User testUser;
    private Authentication testAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sdkDownloadController)
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();

        testUser = new User();
        testUser.setUserId("usr-dev-123");
        testUser.setEmail("dev@fonepay.com");

        testAuth = new UsernamePasswordAuthenticationToken(testUser, null, List.of());
    }

    @ParameterizedTest(name = "Authenticated GET /api/v1/developer/sdks/{0} -> redirects to {1}")
    @CsvSource({
            "java, https://github.com/fonepay/fonepay-sdk-java",
            "php, https://github.com/fonepay/fonepay-sdk-php",
            "python, https://github.com/fonepay/fonepay-sdk-python",
            "javascript, https://github.com/fonepay/fonepay-sdk-js"
    })
    @DisplayName("Valid authenticated user + supported language redirects (302 Found) to correct repository")
    void downloadSdk_AuthenticatedSupportedLanguage_RedirectsCorrectly(String language, String expectedUrl) throws Exception {
        when(sdkDownloadService.resolveDownloadUrl(language)).thenReturn(expectedUrl);

        mockMvc.perform(get("/api/v1/developer/sdks/" + language)
                        .principal(testAuth))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", expectedUrl));
    }

    @Test
    @DisplayName("Unauthenticated request returns 401 Unauthorized and does not expose repository URL")
    void downloadSdk_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/developer/sdks/java"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    @DisplayName("Unsupported language returns 400 Bad Request")
    void downloadSdk_UnsupportedLanguage_ReturnsBadRequest() throws Exception {
        String unsupported = "rust";
        when(sdkDownloadService.resolveDownloadUrl(unsupported))
                .thenThrow(new BadRequestException("Unsupported SDK language: 'rust'"));

        mockMvc.perform(get("/api/v1/developer/sdks/" + unsupported)
                        .principal(testAuth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unsupported SDK language: 'rust'"))
                .andExpect(header().doesNotExist("Location"));
    }
}
