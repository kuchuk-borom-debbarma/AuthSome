package dev.kuku.authsome.cloud.models;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserIdentityType;

public class SignInRequest {
    public String identity;
    public AuthsomeUserIdentityType identityType;
    public String password;
}
