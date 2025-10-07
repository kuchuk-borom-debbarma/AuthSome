package dev.kuku.authsome.cloud.models;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserIdentityType;
import lombok.ToString;

@ToString
public class SignupStartRequest {
    public String identity;
    public AuthsomeUserIdentityType identityType;
    public String username;
    public String password;
}
