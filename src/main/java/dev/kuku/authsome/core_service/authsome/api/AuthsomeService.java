package dev.kuku.authsome.core_service.authsome.api;

import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserIdentityType;
import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserToFetch;
import dev.kuku.authsome.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.authsome.core_service.authsome.api.exceptions.*;
import dev.kuku.authsome.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.authsome.util_service.jwt.api.exception.InvalidJwtToken;
import org.apache.coyote.BadRequestException;
import org.springframework.lang.Nullable;

public interface AuthsomeService {
    /**
     * Start signup process and send OTP to identity.
     *
     * @param identityType type of the identity
     * @param identity     identity
     * @param username     unique username
     * @param password     password to sign-in with
     * @return token that needs to be passed during verification
     */
    String startSignupProcess(AuthsomeUserIdentityType identityType, String identity, String username, String password) throws AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse, InvalidOtpTypeException;

    /**
     * Complete signup process by verifying that the token and the otp matches. If matches will save the user in the database
     *
     * @param token token that was returned during {@link AuthsomeService#startSignupProcess}
     * @param otp   otp that was sent to the identity
     */
    void completeSignupProcess(String token, String otp) throws InvalidJwtToken, ExpiredJwtToken, OtpMismatchException, AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse, OtpNotFoundInDatabase;

    /**
     * Sign in with credential
     *
     * @param identityType type of the identity
     * @param identity     identity logging in with
     * @param password     password
     * @return access token and refresh token pair
     */
    SignInTokens signIn(AuthsomeUserIdentityType identityType, String identity, String password) throws AuthsomeUserWithIdentityNotFound, AuthsomePasswordMismatch, MaxActiveSessionsReached;

    /**
     * Generate new pair of tokens using refreshToken.
     *
     * @param refreshToken valid refresh token
     * @return new access and refresh token pair
     */
    SignInTokens refreshToken(String refreshToken) throws InvalidRefreshToken;

    /**
     * revoke the provided refresh token
     *
     * @param refreshToken refresh token to revoke
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * revoke all refresh token of the provided user
     *
     * @param userId userId whose refresh token needs to be revoked
     */
    void revokeAllRefreshTokenOfUser(String userId);

    /**
     * Get the user by id and/or username
     *
     * @param id       id of the user
     * @param username username of the user
     * @return the fetched user
     */
    @Nullable
    AuthsomeUserToFetch getAuthsomeUser(@Nullable String id, @Nullable String username) throws BadRequestException;

}
