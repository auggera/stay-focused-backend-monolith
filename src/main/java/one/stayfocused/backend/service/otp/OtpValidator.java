package one.stayfocused.backend.service.otp;

public interface OtpValidator {
    void validateOtp(String otpType, String identifier, String otp);
}
