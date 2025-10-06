package dev.kuku.authsome.core_service.authsome.api;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserToFetch;
import dev.kuku.authsome.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.authsome.core_service.authsome.api.exceptions.*;
import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import dev.kuku.authsome.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.authsome.util_service.jwt.api.exception.InvalidJwtToken;
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
    String startSignupProcess(IdentityType identityType, String identity, String username, String password) throws AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse;

    /**
     * Complete signup process by verifying that the token and the otp matches. If matches will save the user in the database
     *
     * @param token
     * @param otp
     */
    void completeSignupProcess(String token, String otp) throws InvalidJwtToken, ExpiredJwtToken, OtpMismatchException, AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse, OtpNotFoundInDatabase;

    /**
     * Sign in with credential
     *
     * @param identityType
     * @param identityValue
     * @param password
     * @return access token
     */
    SignInTokens signIn(IdentityType identityType, String identityValue, String password) throws AuthsomeUserWithIdentityNotFound;

    /**
     * Get the user by id and/or username
     * @param id
     * @param username
     * @return the fetched user
     */
    @Nullable AuthsomeUserToFetch getAuthsomeUser(@Nullable String id,@Nullable String username);

}
