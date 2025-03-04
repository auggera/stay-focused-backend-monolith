package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import one.stayfocused.backend.validation.ValidName;

@Data
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
