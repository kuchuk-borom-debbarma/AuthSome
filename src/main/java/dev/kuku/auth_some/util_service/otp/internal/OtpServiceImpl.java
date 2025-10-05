package dev.kuku.auth_some.util_service.otp.internal;

import dev.kuku.auth_some.util_service.otp.api.OtpRecord;
import dev.kuku.auth_some.util_service.otp.api.OtpService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OtpServiceImpl implements OtpService {
    //Spring handles creation of objects as long as they are annotated properly such as @service, @component, @repo
    private final SimpleOtpRepo repo;

    public OtpServiceImpl(SimpleOtpRepo repo) {
        this.repo = repo;
    }

    @Override
    public int generateNumericOtp(int length) {
        return 0;
    }

    @Override
    public OtpRecord saveOtpWithCustomData(String otp, Map<String, Object> customData) {
        // saving returns us the saved value so we can return it directly after mapping it to otpRecord
        OtpEntity savedRecord = repo.save(new OtpEntity(null, otp, customData, System.currentTimeMillis(), System.currentTimeMillis()));
        return new OtpRecord(savedRecord.otp, savedRecord.otp, savedRecord.customData);
    }
}
