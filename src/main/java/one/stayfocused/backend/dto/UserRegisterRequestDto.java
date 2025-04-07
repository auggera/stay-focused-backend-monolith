package one.stayfocused.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import one.stayfocused.backend.validation.*;

@Data
public class UserRegisterRequestDto {

    @NotBlank
    @ValidEmail
    private String email;

    @NotBlank
    @ValidName
    private String name;

    @NotBlank
    @ValidPassword
    private String password;

    @JsonProperty(defaultValue = "false")
    private boolean emailVerified;
}
