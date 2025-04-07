package one.stayfocused.backend.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record UserAvatarUploadRequestDto(
        @NotBlank MultipartFile file
) {}
