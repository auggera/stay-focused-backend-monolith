package one.stayfocused.backend.dto;

public record PasswordChangeWithOtpRequestDto(
        String otpCode,
        String newPassword
) {}
