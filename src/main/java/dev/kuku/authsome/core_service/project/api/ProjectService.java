package dev.kuku.authsome.core_service.project.api;

import dev.kuku.authsome.core_service.project.api.dto.ProjectToFetch;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityToFetch;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityType;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserToFetch;

import java.util.Optional;

/**
 * Project service that is responsible for handling all project related operations.
 * <p>
 * projects are isolated environments for authsome authsome_user to manage their own users and configurations at project level.
 */
public interface ProjectService {


    ProjectToFetch createProject(String authsomeUserId, String projectName, String description);

    ProjectToFetch updateProject(String authsomeUserId, String projectId, String name, String description);

    ProjectUserToFetch createUserForProject(String authsomeUserId, String projectId, String username, Optional<String> password);

    ProjectUserToFetch updatePassword(String authsomeUserId, String projectId, String userId, String updatedPassword);

    ProjectUserIdentityToFetch addIdentitiesForUsers(String authsomeUserId, String projectId, String userId, ProjectUserIdentityType identityType, String identity, boolean verified, boolean isPrimary);

    ProjectUserIdentityToFetch setIdentityVerified(String authsomeUserId, String projectId, String projectUserId, ProjectUserIdentityType identityType, String identity, boolean verified);
    ProjectUserIdentityToFetch setIdentityPrimary(String authsomeUserId, String projectId, String projectUserId, ProjectUserIdentityType identityType, String identity, boolean isPrimary);
}
