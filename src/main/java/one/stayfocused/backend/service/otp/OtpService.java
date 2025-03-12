package one.stayfocused.backend.service;

public interface OtpService {

    String generateOtp(String otpType, String identifier);
    void validateOtp(String otpType, String identifier, String otp);
    void deleteOtp(String otpType, String identifier);
}
