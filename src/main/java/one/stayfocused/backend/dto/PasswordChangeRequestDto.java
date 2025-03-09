package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequestDto (
        @NotBlank String currentPassword
) {}
