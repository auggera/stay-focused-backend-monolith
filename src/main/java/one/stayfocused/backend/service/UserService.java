package one.stayfocused.backend.service;

import one.stayfocused.backend.dto.UserResponseDto;
import one.stayfocused.backend.model.RoleType;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto updateName(Long id, String newName);
    UserResponseDto updateAvatar(Long id, String avatarUrl);
    UserResponseDto uploadAvatar(Long id, MultipartFile file);
    void deleteUser(Long id);
    void assignRole(Long userId, RoleType roleType);
}

