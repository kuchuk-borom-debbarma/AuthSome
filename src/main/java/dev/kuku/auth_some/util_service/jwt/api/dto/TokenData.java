package dev.kuku.auth_some.util_service.jwt.api.dto;

import java.util.Map;

public record TokenData(String subject, Map<String,Object> claims) {
}
