package dev.kuku.authsome.cloud.models.project;

import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityType;
import lombok.ToString;

@ToString
public class StartIdentityVerificationForUserRequest {
    public String projectId;
    public String userId;
    public String identity;
    public ProjectUserIdentityType identityType;
}
