package com.fonepay.devportal.modules.developer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.modules.developer.service.impl.SdkDownloadServiceImpl;

class SdkDownloadServiceTest {

    private SdkDownloadService sdkDownloadService;

    @BeforeEach
    void setUp() {
        sdkDownloadService = new SdkDownloadServiceImpl();
    }

    @ParameterizedTest(name = "Language {0} resolves to {1}")
    @CsvSource({
            "java, https://github.com/fonepay/fonepay-sdk-java",
            "php, https://github.com/fonepay/fonepay-sdk-php",
            "python, https://github.com/fonepay/fonepay-sdk-python",
            "javascript, https://github.com/fonepay/fonepay-sdk-js",
            "JAVA, https://github.com/fonepay/fonepay-sdk-java",
            "Php, https://github.com/fonepay/fonepay-sdk-php",
            "Python, https://github.com/fonepay/fonepay-sdk-python",
            "JavaScript, https://github.com/fonepay/fonepay-sdk-js"
    })
    @DisplayName("Each supported language maps to the correct repository URL")
    void resolveDownloadUrl_SupportedLanguages_ReturnsCorrectUrl(String language, String expectedUrl) {
        String resolvedUrl = sdkDownloadService.resolveDownloadUrl(language);
        assertNotNull(resolvedUrl);
        assertEquals(expectedUrl, resolvedUrl);
    }

    @ParameterizedTest(name = "Unsupported language: {0}")
    @ValueSource(strings = {"ruby", "golang", "csharp", "rust", "swift", "kotlin", "invalid", "", "   "})
    @DisplayName("Unsupported language throws BadRequestException (400)")
    void resolveDownloadUrl_UnsupportedLanguage_ThrowsBadRequestException(String language) {
        assertThrows(BadRequestException.class, () ->
                sdkDownloadService.resolveDownloadUrl(language)
        );
    }

    @Test
    @DisplayName("Null language throws BadRequestException (400)")
    void resolveDownloadUrl_NullLanguage_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () ->
                sdkDownloadService.resolveDownloadUrl(null)
        );
    }
}
