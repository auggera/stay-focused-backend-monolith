package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import one.stayfocused.backend.validation.ValidEmail;
import one.stayfocused.backend.validation.ValidName;

@Data
public class UserUpdateRequestDto {

    @NotBlank
    @ValidEmail
    private String email;

    @NotBlank
    @ValidName
    private String name;
}
