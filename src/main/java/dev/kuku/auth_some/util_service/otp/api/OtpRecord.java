package dev.kuku.auth_some.util_service.otp.api;

import java.util.Map;

public class OtpRecord {
    public String id;
    public String otp;
    public Map<String, Object> customData;

    public OtpRecord(String id, String otp, Map<String, Object> customData) {
        this.id = id;
        this.otp = otp;
        this.customData = customData;
    }
}
