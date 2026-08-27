package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.dto.request.AcceptInviteRequest;
import com.fonepay.devportal.modules.auth.dto.response.InvitePreviewResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;

public interface InviteAcceptanceService {

    InvitePreviewResponse previewInvite(String token);

    RegistrationResponse acceptInvite(AcceptInviteRequest request);
}
