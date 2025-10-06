package dev.kuku.authsome.util_service.notifier.impl;

import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import dev.kuku.authsome.util_service.notifier.api.NotifierService;
import org.springframework.stereotype.Service;

@Service
public class NotifierServiceImpl implements NotifierService {
    @Override
    public void sendNotificationToIdentity(IdentityType identityType, String identity, String subject, String content) {

    }
}
