package dev.kuku.auth_some.cloud.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthSomeRestController {
    //1. Endpoint to signup with username, identity, password. Should send OTP to identity
    //2. Endpoint to complete signup with code
    //3. Endpoint to sign in and get back jwt tokens
    //4. Endpoint to refresh token
    //5. Endpoint to start password reset (send OTP to identity)
    //6. Endpoint to complete password reset (with OTP and new password)
}
