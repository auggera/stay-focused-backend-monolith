package one.stayfocused.backend.service;

import one.stayfocused.backend.dto.*;

public interface PasswordService {
    void verifyCurrentPassword(Long userId, PasswordVerificationRequestDto request);
    void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request);

    void requestChangePasswordWithOtp(Long userId);
    void changePasswordWithOtp(Long userId, PasswordChangeWithOtpRequestDto request);

    void requestResetPasswordWithOtp(PasswordResetRequestDto request);
    void resetPasswordWithOtp(PasswordResetWithOtpRequestDto request);
}
