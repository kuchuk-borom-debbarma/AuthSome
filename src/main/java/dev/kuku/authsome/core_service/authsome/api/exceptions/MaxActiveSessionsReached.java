package dev.kuku.authsome.core_service.authsome.api.exceptions;

public class MaxActiveSessionsReached extends AuthsomeException {
    public MaxActiveSessionsReached(String userId, int maxActiveSession) {
    }
}
