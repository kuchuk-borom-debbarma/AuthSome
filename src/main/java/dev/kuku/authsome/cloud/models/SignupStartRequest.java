package dev.kuku.authsome.cloud.models;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import lombok.ToString;

@ToString
public class SignupStartRequest {
    public String identity;
    public IdentityType identityType;
    public String username;
    public String password;
}
