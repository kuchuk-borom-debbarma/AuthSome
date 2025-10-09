package dev.kuku.authsome.core_service.project.impl;

import dev.kuku.authsome.core_service.project.api.ProjectService;
import dev.kuku.authsome.core_service.project.api.dto.ProjectToFetch;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityToFetch;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserIdentityType;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserToFetch;
import dev.kuku.vfl.api.annotation.SubBlock;
import dev.kuku.vfl.api.annotation.VFLAnnotation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final VFLAnnotation log;

    @Override
    @SubBlock
    public ProjectToFetch createProject(String authsomeUserId, String projectName, String description) {
        log.info("createProject( {}, {}, {})", authsomeUserId, projectName, description);
        return null;
    }

    @Override
    @SubBlock
    public ProjectToFetch updateProject(String authsomeUserId, String projectId, String name, String description) {
        log.info("updateProject( {}, {}, {})", authsomeUserId, projectId, name);
        return null;
    }

    @Override
    public ProjectUserToFetch createUserForProject(String authsomeUserId, String projectId, String username, Optional<String> password) {
        log.info("createUserForProject( {}, {}, {}, {})", authsomeUserId, projectId, username, password.isPresent() ? password.get().substring(2, 4) : "NO_PWD");
        return null;
    }

    @Override
    public ProjectUserToFetch updatePassword(String authsomeUserId, String projectId, String userId, String updatedPassword) {
        log.info("updatePassword( {}, {}, {}, {})", authsomeUserId, projectId, userId, authsomeUserId);
        return null;
    }

    @Override
    public ProjectUserIdentityToFetch addIdentitiesForUsers(String authsomeUserId, String projectId, String userId, ProjectUserIdentityType identityType, String identity, boolean verified, boolean isPrimary) {
        log.info("addIdentitiesForUsers({}, {}, {}, {}, {}, {}, {})", authsomeUserId, projectId, userId, identityType, identity, verified, isPrimary);
        return null;
    }

    @Override
    public ProjectUserIdentityToFetch setIdentityVerified(String authsomeUserId, String projectId, String projectUserId, ProjectUserIdentityType identityType, String identity, boolean verified) {
        log.info("setIdentityVerified {}, {}, {}, {}, {}, {}", authsomeUserId, projectId, projectUserId, identityType, identity, verified);
        return null;
    }

    @Override
    public ProjectUserIdentityToFetch setIdentityPrimary(String authsomeUserId, String projectId, String projectUserId, ProjectUserIdentityType identityType, String identity, boolean isPrimary) {
        log.info("setIdentityPrimary {}, {}, {}, {}, {}", authsomeUserId, projectId, projectUserId, identityType, identity, isPrimary);
        return null;
    }
}
