package dev.kuku.auth_some.util_service.otp.api;

import dev.kuku.auth_some.util_service.otp.api.dto.OtpToFetch;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface OtpService {
    int generateNumericOtp(int length);

    String generateAlphaNumericOtp(int length, int minAlphabets, int minNumbers);

    OtpToFetch saveOtpWithCustomData(String otp, Map<String, Object> customData, int expiresAt, TimeUnit timeUnit);

    @Nullable OtpToFetch getOtpById(String id);

    void deleteById(String id);
}
