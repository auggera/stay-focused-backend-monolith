package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import one.stayfocused.backend.validation.ValidName;

public record UserNameUpdateRequestDto(
        @NotBlank @ValidName String newName
) {}