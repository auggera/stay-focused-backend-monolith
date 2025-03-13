package one.stayfocused.backend.service.password;

import one.stayfocused.backend.dto.PasswordChangeWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordResetRequestDto;
import one.stayfocused.backend.dto.PasswordResetWithOtpRequestDto;

public interface PasswordOtpService {
    void requestChangePasswordWithOtp(Long userId);
    void changePasswordWithOtp(Long userId, PasswordChangeWithOtpRequestDto request);

    void requestResetPasswordWithOtp(PasswordResetRequestDto request);
    void resetPasswordWithOtp(PasswordResetWithOtpRequestDto request);
}
