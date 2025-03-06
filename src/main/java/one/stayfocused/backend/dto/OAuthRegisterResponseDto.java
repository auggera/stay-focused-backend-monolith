package one.stayfocused.backend.dto;

import lombok.Data;

@Data
public class OAuthRegisterResponseDto {

    private String email;
    private String name;
    private String accessToken;
}
