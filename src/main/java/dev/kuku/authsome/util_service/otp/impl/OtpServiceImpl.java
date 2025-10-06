package dev.kuku.authsome.util_service.otp.impl;

import dev.kuku.authsome.util_service.otp.api.OtpService;
import dev.kuku.authsome.util_service.otp.api.dto.OtpToFetch;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {
    @Override
    public int generateNumericOtp(int length) {
        return 0;
    }

    @Override
    public String generateAlphaNumericOtp(int length, int minAlphabets, int minNumbers) {
        return "";
    }

    @Override
    public String generateAlphabeticOtp(int otpLength) {
        return "";
    }

    @Override
    public OtpToFetch saveOtpWithCustomData(String otp, Map<String, Object> customData, int expiresAt, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public OtpToFetch getOtpById(String id) {
        return null;
    }

    @Override
    public void deleteById(String id) {

    }
}
