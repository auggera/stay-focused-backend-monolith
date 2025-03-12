package one.stayfocused.backend.service;

public interface OtpGenerator {
    String generateOtp(String otpType, String identifier);
}
