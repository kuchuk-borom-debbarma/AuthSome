package dev.kuku.authsome.cloud.models;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;

public class SignInRequest {
    public String identity;
    public IdentityType identityType;
    public String password;
}
