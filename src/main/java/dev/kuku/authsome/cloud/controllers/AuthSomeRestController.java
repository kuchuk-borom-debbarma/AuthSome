package dev.kuku.authsome.cloud.controllers;

import dev.kuku.authsome.cloud.models.ResponseModel;
import dev.kuku.authsome.cloud.models.SignInRequest;
import dev.kuku.authsome.cloud.models.SignupStartRequest;
import dev.kuku.authsome.cloud.models.VerifySignupRequest;
import dev.kuku.authsome.core_service.authsome.api.AuthsomeService;
import dev.kuku.authsome.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.authsome.core_service.authsome.api.exceptions.*;
import dev.kuku.authsome.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.authsome.util_service.jwt.api.exception.InvalidJwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthSomeRestController {
    private final AuthsomeService authsomeService;

    //1. Endpoint to signup with username, identity, password. Should send OTP to identity
    @PostMapping("/signup-start")
    public ResponseModel<String> startSignup(@RequestBody SignupStartRequest body) throws AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse {
        log.trace("signup-start {}", body);
        /*
        1. Receive: identity_type, identity, username, password
        2. Generate OTP + save with user data
        3. Generate token (subject = OTP record ID)
        4. Return token + send OTP via notification
         */
        String token = authsomeService.startSignupProcess(body.identityType, body.identity, body.username, body.password);
        return new ResponseModel<>(token, null);
    }

    //2. Endpoint to complete signup with code
    @PostMapping("/signup-verify")
    public ResponseModel<Void> verifySignup(@RequestHeader("X-Verification-Token") String token, @RequestBody VerifySignupRequest body) throws InvalidJwtToken, AuthsomeUsernameAlreadyInUse, OtpMismatchException, AuthsomeIdentityAlreadyInUse, ExpiredJwtToken, OtpNotFoundInDatabase {
        log.trace("verifySignup");
        /*
        1. Get the id of the record from the token
        2. Verify against passed otp
        3. If matches store user info in database
         */
        authsomeService.completeSignupProcess(token, body.otp);
        return new ResponseModel<>(null, null);
    }

    //3. Endpoint to sign in and get back jwt tokens
    @PostMapping("/signin")
    public ResponseModel<SignInTokens> signIn(@RequestBody SignInRequest body) throws AuthsomeUserWithIdentityNotFound {
        log.trace("signin {}", body);
        SignInTokens tokens = authsomeService.signIn(body.identityType, body.identity, body.password);
        return new ResponseModel<>(tokens, null);
    }
    //4. Endpoint to refresh token
    //5. Endpoint to start password reset (send OTP to identity)
    //6. Endpoint to complete password reset (with OTP and new password)
}
