package one.stayfocused.backend.dto;

import one.stayfocused.backend.model.RoleType;

public record UserRoleAssignmentRequestDto(
        RoleType roleType
) {}

