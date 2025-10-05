package dev.kuku.auth_some.core_service.project.api;

import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;
import dev.kuku.auth_some.core_service.project.api.dto.ProjectToFetch;
import dev.kuku.auth_some.core_service.project.api.dto.ProjectUserToAdd;

import java.util.List;

/**
 * Project service that is responsible for handling all project related operations.
 * <p>
 * projects are isolated environments for authsome authsome_user to manage their own users and configurations at project level.
 */
public interface ProjectService {
    /**
     * Create a project for an authsome authsome_user.
     *
     * @param authsomeUserId    id of the authsome authsome_user
     * @param uniqueProjectName unique name of the project
     */
    void createProject(String authsomeUserId, String uniqueProjectName);

    /**
     * Add users to a project.
     *
     * @param projectId        id of the project.
     * @param projectUsernames list of usernames to be added to the project.
     */
    void addUsersToProject(String projectId, List<ProjectUserToAdd> projectUsernames);

    /**
     * Add (update if exists) password of a authsome_user without any verification.
     *
     * @param projectId
     * @param projectUsername
     * @param password
     */
    void upsertPasswordForProjectUser(String projectId, String projectUsername, String password);

    /**
     * Update the password of a authsome_user if current password matches
     *
     * @param projectId       id of the project.
     * @param projectUsername username of the project's authsome_user.
     * @param currentPassword current password of the authsome_user (can be null. If null then will directly set the new password)
     * @param newPassword     new password of the project authsome_user.
     */
    void updatePasswordForProjectUser(String projectId, String projectUsername, String currentPassword, String newPassword);

    /**
     * Start update password process
     *
     * @param projectId
     * @param projectUsername
     * @param identityType
     * @param identity
     * @return token that must be sent along with the OTP during verification process.
     */
    String updatePasswordWithIdentityOtp(String projectId, String projectUsername, IdentityType identityType, String identity);

    /**
     * Verify the OTP and set new password for the given project authsome_user.
     *
     * @param projectId
     * @param projectUsername
     * @param token
     * @param otp
     * @param newPassword
     */
    void verifyUpdatePasswordWithidentityOtp(String projectId, String projectUsername, String token, String otp, String newPassword);

    /**
     * Add identity for a authsome_user in a project.
     *
     * @param projectId        id of the project.
     * @param projectUsername  username of the authsome_user in the project.
     * @param identityProvider identity provider type.
     * @param identity         identity value from the identity provider.
     * @param isVerified       if true the identity will be persisted as verified and will not require further verification.
     */
    void addIdentityForUser(String projectId, String projectUsername, IdentityType identityProvider, String identity, boolean isVerified);

    /**
     * Start identity verification process for a authsome_user in a project using OTP as verification method.
     *
     * @param projectId        id of the project.
     * @param projectUsername  username of the authsome_user in the project.
     * @param identityProvider identity provider type.
     * @param identity         identity value from the identity provider.
     * @return token that must be passed during verification of the identity.
     */
    String startidentityVerificationWithOtp(String projectId, String projectUsername, IdentityType identityProvider, String identity);

    /**
     * Verify identity for a authsome_user in a project using OTP as verification method. Will throw except if something went wrong.
     *
     * @param projectId        id of the project.
     * @param projectUsername  username of the authsome_user in the project.
     * @param identityProvider identity provider type.
     * @param identity         identity value from the identity provider.
     * @param otp              one time password sent to the identity.
     * @param token            token received when starting the identity verification process.
     */
    void verifyIdentityWithOtp(String projectId, String projectUsername, IdentityType identityProvider, String identity, String otp, String token);

    /**
     * Get all the projects of a user
     *
     * @return
     */
    List<ProjectToFetch> getProjectOfUser(String userId, String cursor, int limit);

    //TODO Get based methods not defined yet. Will do as per requirements later down the line

}
