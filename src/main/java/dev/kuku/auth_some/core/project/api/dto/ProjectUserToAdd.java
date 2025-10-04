package dev.kuku.auth_some.core.project.api.dto;

import org.springframework.lang.Nullable;

public record ProjectUserToAdd(String username, @Nullable String password) {
}
