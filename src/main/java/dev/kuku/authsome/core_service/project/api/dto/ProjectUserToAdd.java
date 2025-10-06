package dev.kuku.authsome.core_service.project.api.dto;

import org.springframework.lang.Nullable;

public record ProjectUserToAdd(String username, @Nullable String password) {
}
