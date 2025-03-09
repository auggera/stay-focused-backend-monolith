package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotNull;
import one.stayfocused.backend.model.RoleType;

public record UserRoleAssignmentRequestDto(
        @NotNull RoleType roleType
) {}

