package com.fonepay.devportal.modules.admin.invitation.service;

import com.fonepay.devportal.modules.admin.invitation.dto.request.CreateInvitationRequest;
import com.fonepay.devportal.modules.admin.invitation.dto.response.InvitationResponse;

public interface InvitationService {

    InvitationResponse invite(CreateInvitationRequest request, String invitedByUserId);
}
