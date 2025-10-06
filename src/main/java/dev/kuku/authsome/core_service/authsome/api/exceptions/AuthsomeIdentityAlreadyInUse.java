package dev.kuku.authsome.core_service.authsome.api.exceptions;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;

public class AuthsomeIdentityAlreadyInUse extends AuthsomeException {
    public AuthsomeIdentityAlreadyInUse(IdentityType identityType, String identity) {
    }
}
