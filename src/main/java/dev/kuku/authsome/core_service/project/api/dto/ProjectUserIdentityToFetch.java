package dev.kuku.authsome.core_service.project.api.dto;

public record ProjectUserIdentityToFetch(String id, ProjectUserIdentityType identityType, String identity,
                                         boolean verified, String projectId, String userId) {
}
