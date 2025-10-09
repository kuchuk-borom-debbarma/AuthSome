package dev.kuku.authsome.cloud.models.project;

import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityType;
import lombok.ToString;

@ToString
public class AddIdentityForUserRequest {
    public String projectId;
    public String userId;
    public ProjectUserIdentityType identityType;
    public String identity;
    public boolean verified;
    public boolean isPrimary;
}
