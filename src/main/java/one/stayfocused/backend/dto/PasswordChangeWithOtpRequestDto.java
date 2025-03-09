package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import one.stayfocused.backend.validation.ValidPassword;

public record PasswordChangeWithOtpRequestDto(

        @NotBlank String otpCode,
        @NotBlank @ValidPassword String newPassword
) {}
