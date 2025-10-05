package dev.kuku.auth_some.util_service.jwt.api;

import dev.kuku.auth_some.util_service.jwt.api.dto.TokenData;
import dev.kuku.auth_some.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.auth_some.util_service.jwt.api.exception.InvalidJwtToken;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface JwtService {
    String generateToken(String subject, Map<String, Object> claims, int expiresAt, TimeUnit timeUnit);

    TokenData extractToken(String token) throws InvalidJwtToken, ExpiredJwtToken;
}
