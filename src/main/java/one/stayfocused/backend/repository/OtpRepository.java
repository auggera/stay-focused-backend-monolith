package one.stayfocused.backend.repository;

import java.time.Duration;

public interface OtpRepository {
    void saveOtp(String otpType, String identifier, String otp, Duration expiration);
    String getOtp(String otpType, String identifier);
    void deleteOtp(String otpType, String identifier);

    int getFailedAttempts(String otpType, String identifier);
    void incrementFailedAttempts(String otpType, String identifier, Duration blockDuration);

    int getRequestCount(String otpType, String identifier);
    void incrementRequestCount(String otpType, String identifier, Duration requestLimitDuration);

    Duration getOtpExpiration(String otpType);
}
