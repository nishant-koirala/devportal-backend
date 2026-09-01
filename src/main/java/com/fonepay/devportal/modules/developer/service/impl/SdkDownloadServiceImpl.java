package com.fonepay.devportal.modules.developer.service.impl;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.modules.developer.constant.SdkConstants;
import com.fonepay.devportal.modules.developer.service.SdkDownloadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SdkDownloadServiceImpl implements SdkDownloadService {

    @Override
    public String resolveDownloadUrl(String language) {
        log.info("Resolving SDK download URL for language: [{}]", language);

        if (!SdkConstants.isSupported(language)) {
            log.warn("Unsupported SDK language requested: [{}]", language);
            throw new BadRequestException("Unsupported SDK language: '" + language + "'. Supported languages are: "
                    + SdkConstants.SUPPORTED_LANGUAGES);
        }

        String url = SdkConstants.getRepositoryUrl(language);
        log.info("Resolved SDK download URL for language [{}] to [{}]", language, url);
        return url;
    }
}
