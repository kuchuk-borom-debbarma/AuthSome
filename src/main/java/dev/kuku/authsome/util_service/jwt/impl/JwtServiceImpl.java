package dev.kuku.authsome.util_service.jwt.impl;

import dev.kuku.authsome.util_service.jwt.api.JwtService;
import dev.kuku.authsome.util_service.jwt.api.dto.TokenData;
import dev.kuku.authsome.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.authsome.util_service.jwt.api.exception.InvalidJwtToken;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtServiceImpl implements JwtService {
    @Override
    public String generateToken(String subject, Map<String, Object> claims, int expiresAt, TimeUnit timeUnit) {
        return "";
    }

    @Override
    public TokenData extractToken(String token) throws InvalidJwtToken, ExpiredJwtToken {
        return null;
    }
}
