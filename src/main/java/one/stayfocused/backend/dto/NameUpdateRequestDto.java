package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import one.stayfocused.backend.validation.ValidName;

public record NameUpdateRequestDto(
        @NotBlank
        @ValidName
        String newName
) {}