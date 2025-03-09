package one.stayfocused.backend.service;

public interface PasswordService {
    void verifyCurrentPassword(Long userId, String currentPassword);
    void changePasswordAfterVerification(Long userId, String newPassword);
    void requestChangePasswordWithOtp(Long userId);
    void changePasswordWithOtp(Long userId, String otpCode, String newPassword);
    void requestResetPasswordWithOtp(String email);
    void resetPasswordWithOtp(String email, String otpCode, String newPassword);
}
