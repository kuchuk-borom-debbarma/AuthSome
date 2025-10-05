package dev.kuku.auth_some.util_service.jwt.api;

import java.util.Map;

public interface JwtService {
    String generateToken(String subject, Map<String, Object> claims, long expiry);
}
