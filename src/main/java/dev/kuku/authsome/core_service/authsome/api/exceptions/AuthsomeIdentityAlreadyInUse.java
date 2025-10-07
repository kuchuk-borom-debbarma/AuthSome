package dev.kuku.authsome.core_service.authsome.api.exceptions;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserIdentityType;

public class AuthsomeIdentityAlreadyInUse extends AuthsomeException {
    public AuthsomeIdentityAlreadyInUse(AuthsomeUserIdentityType identityType, String identity) {
    }
}
