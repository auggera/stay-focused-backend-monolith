package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import one.stayfocused.backend.validation.ValidPassword;

@Data
public class ChangePasswordRequestDto {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @ValidPassword
    private String newPassword;
}
