package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import one.stayfocused.backend.validation.ValidName;

public class OAuthRegisterRequestDto {

    private String email;

    @NotBlank
    @ValidName
    private String name;

    @NotBlank
    private String provider;

    @NotBlank
    private String providerId;
}
