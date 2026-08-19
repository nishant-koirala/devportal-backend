package com.fonepay.devportal.modules.auth.policy;

import java.util.List;

import com.fonepay.devportal.modules.user.document.User;

public interface MfaPolicy {

    boolean isMfaRequired(User user, List<String> roleNames);
}
