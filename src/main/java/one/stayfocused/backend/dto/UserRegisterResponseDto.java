package one.stayfocused.backend.dto;

public record UserRegisterResponseDto(
        String email,
        String name,
        boolean emailVerified
) {}

