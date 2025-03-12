package one.stayfocused.backend.service;

public interface OtpValidator {
    void validateOtp(String otpType, String identifier, String otp);
}
