package dev.kuku.authsome.core_service.project.internal.service;

import dev.kuku.authsome.core_service.project.api.ProjectService;
import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import dev.kuku.authsome.core_service.project.api.dto.ProjectUserToAdd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {
    @Override
    public void createProject(String authsomeUserId, String uniqueProjectName) {
    }

    @Override
    public void addUsersToProject(String projectId, List<ProjectUserToAdd> projectUsernames) {

    }

    @Override
    public void upsertPasswordForProjectUser(String projectId, String projectUsername, String password) {

    }

    @Override
    public void updatePasswordForProjectUser(String projectId, String projectUsername, String currentPassword, String newPassword) {

    }

    @Override
    public String updatePasswordWithIdentityOtp(String projectId, String projectUsername, IdentityType identityType, String identity) {
        return "";
    }

    @Override
    public void verifyUpdatePasswordWithidentityOtp(String projectId, String projectUsername, String token, String otp, String newPassword) {

    }

    @Override
    public void addIdentityForUser(String projectId, String projectUsername, IdentityType identityProvider, String identity, boolean isVerified) {

    }

    @Override
    public String startidentityVerificationWithOtp(String projectId, String projectUsername, IdentityType identityProvider, String identity) {
        return "";
    }

    @Override
    public void verifyIdentityWithOtp(String projectId, String projectUsername, IdentityType identityProvider, String identity, String otp, String token) {

    }
}
