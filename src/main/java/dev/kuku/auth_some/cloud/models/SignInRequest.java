package dev.kuku.auth_some.cloud.models;

import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;

public class SignInRequest {
    public String identity;
    public IdentityType identityType;
    public String password;
}
