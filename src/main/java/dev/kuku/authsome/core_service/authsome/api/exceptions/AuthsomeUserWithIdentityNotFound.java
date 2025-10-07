package dev.kuku.authsome.core_service.authsome.api.exceptions;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserIdentityType;

public class AuthsomeUserWithIdentityNotFound extends AuthsomeException {
    public AuthsomeUserWithIdentityNotFound(AuthsomeUserIdentityType identityType, String identityValue) {
    }
}
