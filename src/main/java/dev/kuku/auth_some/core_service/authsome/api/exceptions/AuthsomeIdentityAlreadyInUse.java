package dev.kuku.auth_some.core_service.authsome.api.exceptions;

import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;

public class AuthsomeIdentityAlreadyInUse extends AuthsomeException {
    public AuthsomeIdentityAlreadyInUse(IdentityType identityType, String identity) {
    }
}
