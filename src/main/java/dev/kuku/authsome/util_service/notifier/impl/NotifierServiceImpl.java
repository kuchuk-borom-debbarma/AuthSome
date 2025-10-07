package dev.kuku.authsome.util_service.notifier.impl;

import dev.kuku.authsome.util_service.notifier.api.NotifierService;
import dev.kuku.authsome.util_service.notifier.api.dto.NotifierIdentityType;
import org.springframework.stereotype.Service;

@Service
public class NotifierServiceImpl implements NotifierService {
    @Override
    public void sendNotificationToIdentity(NotifierIdentityType identityType, String identity, String subject, String content) {

    }
}
