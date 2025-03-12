package one.stayfocused.backend.service;

public interface EmailService {
    void sendOtp(String email, String otpCode);
}
