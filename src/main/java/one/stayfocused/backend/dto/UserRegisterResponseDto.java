package one.stayfocused.backend.dto;

import lombok.Data;

@Data
public class UserRegisterResponseDto {

    private String email;
    private String name;
    private boolean emailVerified;
}
