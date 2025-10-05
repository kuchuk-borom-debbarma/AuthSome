package dev.kuku.auth_some.util_service.notifier.api;

import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;

public interface NotifierService {
    void sendNotificationToIdentity(IdentityType identityType, String identity, String subject, String content);
}
