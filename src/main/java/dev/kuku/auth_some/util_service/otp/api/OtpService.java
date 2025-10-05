package dev.kuku.auth_some.util_service.otp.api;

import java.util.Map;
//interface where we declare the contracts
public interface OtpService {
    int generateNumericOtp(int length);

    OtpRecord saveOtpWithCustomData(String otp, Map<String, Object> customData);
}
