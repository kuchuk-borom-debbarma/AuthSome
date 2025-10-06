package dev.kuku.authsome.util_service.notifier.api;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;

public interface NotifierService {
    void sendNotificationToIdentity(IdentityType identityType, String identity, String subject, String content);
}
