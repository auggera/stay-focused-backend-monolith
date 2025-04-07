package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UserAvatarUpdateRequestDto(
        @NotBlank String avatarUrl
) {}

