package one.stayfocused.backend.service.password;

import one.stayfocused.backend.dto.*;

public interface PasswordService {
    void verifyCurrentPassword(Long userId, PasswordVerificationRequestDto request);
    void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request);
}
