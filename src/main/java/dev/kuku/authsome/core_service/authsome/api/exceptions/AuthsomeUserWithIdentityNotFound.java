package dev.kuku.authsome.core_service.authsome.api.exceptions;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;

public class AuthsomeUserWithIdentityNotFound extends AuthsomeException{
    public AuthsomeUserWithIdentityNotFound(IdentityType identityType, String identityValue) {
    }
}
