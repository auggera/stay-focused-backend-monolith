package one.stayfocused.backend.service.otp;

public interface OtpRateLimiter {
    boolean isBlocked(String otpType, String identifier);
    boolean isRequestLimitExceeded(String otpType, String identifier);
    void incrementFailedAttempts(String otpType, String identifier);
    void incrementOtpRequestCount(String otpType, String identifier);
}
