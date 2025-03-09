package one.stayfocused.backend.service;

import one.stayfocused.backend.dto.PasswordChangeWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordResetWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordVerificationRequestDto;
import one.stayfocused.backend.dto.PasswordUpdateRequestDto;

public interface PasswordService {
    void verifyCurrentPassword(Long userId, PasswordVerificationRequestDto request);
    void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request);

    void requestChangePasswordWithOtp(Long userId);
    void changePasswordWithOtp(Long userId, PasswordChangeWithOtpRequestDto request);

    void requestResetPasswordWithOtp(String email);
    void resetPasswordWithOtp(PasswordResetWithOtpRequestDto request);
}
