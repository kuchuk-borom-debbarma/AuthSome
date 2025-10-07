package dev.kuku.authsome.core_service.authsome.api.exceptions;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserIdentityType;

public class AuthsomePasswordMismatch extends AuthsomeException {
    public AuthsomePasswordMismatch(AuthsomeUserIdentityType identityType, String identityValue) {
    }
}
