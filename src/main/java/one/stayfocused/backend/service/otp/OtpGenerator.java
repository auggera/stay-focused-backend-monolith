package one.stayfocused.backend.service.otp;

public interface OtpGenerator {
    String generateOtp(String otpType, String identifier);
}
