package dev.kuku.auth_some.core_service.authsome.api;

import dev.kuku.auth_some.core_service.authsome.api.dto.AuthsomeUserToFetch;
import dev.kuku.auth_some.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;
import org.springframework.lang.Nullable;

public interface AuthsomeService {
    /**
     * Start signup process and send OTP to identity.
     *
     * @param identityType
     * @param identity
     * @param username
     * @param password
     * @return token that needs to be passed during verification
     */
    String startSignupProcess(IdentityType identityType, String identity, String username, String password);

    /**
     * Complete signup process by verifying that the token and the otp matches. If matches will save the user in the database
     *
     * @param token
     * @param otp
     */
    void completeSignupProcess(String token, String otp);

    /**
     * Sign in with credential
     *
     * @param identityType
     * @param identityValue
     * @param password
     * @return access token
     */
    SignInTokens signin(IdentityType identityType, String identityValue, String password);

    /**
     * Get the user by id and/or username
     * @param id
     * @param username
     * @return the fetched user
     */
    @Nullable AuthsomeUserToFetch getAuthsomeUser(@Nullable String id,@Nullable String username);

}
