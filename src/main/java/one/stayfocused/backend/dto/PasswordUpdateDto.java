package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import one.stayfocused.backend.validation.ValidPassword;

public record PasswordUpdateDto(
    @NotBlank String token,
    @NotBlank @ValidPassword String newPassword
) {}
