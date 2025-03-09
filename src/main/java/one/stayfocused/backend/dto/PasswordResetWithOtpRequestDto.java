package one.stayfocused.backend.dto;

public record PasswordResetWithOtpRequestDto(
        String email,
        String otpCode,
        String newPassword
) {}
