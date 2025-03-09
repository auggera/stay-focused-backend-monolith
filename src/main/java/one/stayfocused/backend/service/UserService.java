package one.stayfocused.backend.service;

import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.model.RoleType;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto updateName(Long id, UserNameUpdateRequestDto request);
    UserResponseDto updateAvatar(Long id, UserAvatarUpdateRequestDto request);
    UserResponseDto uploadAvatar(Long id, UserAvatarUploadRequestDto request);
    void deleteUser(Long id);
    void assignRole(Long userId, UserRoleAssignmentRequestDto request);
}

