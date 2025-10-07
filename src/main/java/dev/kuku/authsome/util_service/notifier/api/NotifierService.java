package dev.kuku.authsome.util_service.notifier.api;


import dev.kuku.authsome.util_service.notifier.api.dto.NotifierIdentityType;

public interface NotifierService {
    void sendNotificationToIdentity(NotifierIdentityType notifierIdentityType, String identity, String subject, String content);
}
