package dev.kuku.authsome.util_service.otp.api.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OtpToFetch {
    public String id;
    public String otp;
    public Map<String, Object> customData;
}
