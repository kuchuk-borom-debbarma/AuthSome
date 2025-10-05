package dev.kuku.auth_some.cloud.models;

import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;
import lombok.ToString;

@ToString
public class SignupStartRequest {
    public String identity;
    public IdentityType identityType;
    public String username;
    public String password;
}
