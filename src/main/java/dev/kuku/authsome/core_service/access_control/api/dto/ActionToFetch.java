package dev.kuku.authsome.core_service.access_control.api.dto;

public record ActionToFetch(String id, String action) {
    public static record ActionAndResource(String actionId, String resourceId) {
    }
}
