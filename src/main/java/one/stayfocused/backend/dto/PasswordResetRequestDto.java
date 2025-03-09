package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestDto(
        @NotBlank String email
) {}