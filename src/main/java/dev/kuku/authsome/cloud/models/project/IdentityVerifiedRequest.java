package dev.kuku.authsome.cloud.models.project;

import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityType;
import lombok.ToString;

@ToString
public class IdentityVerifiedRequest {
    public String projectUserId;
    public String identity;
    public ProjectUserIdentityType identityType;
    public boolean verified;
    public String projectId;
}
