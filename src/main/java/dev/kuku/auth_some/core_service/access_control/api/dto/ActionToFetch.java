package dev.kuku.auth_some.core_service.access_control.api.dto;

public record ActionToFetch(String id, String action) {
    public static record ActionAndResource(String actionId, String resourceId) {
    }
}
