package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordVerificationRequestDto(
        @NotBlank String currentPassword
) {}
