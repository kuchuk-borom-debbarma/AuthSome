package dev.kuku.authsome.core_service.authsome.api.exceptions;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;

public class AuthsomePasswordMismatch extends AuthsomeException{
    public AuthsomePasswordMismatch(IdentityType identityType, String identityValue) {
    }
}
