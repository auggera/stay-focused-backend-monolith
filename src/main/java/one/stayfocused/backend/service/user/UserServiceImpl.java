package one.stayfocused.backend.service.user;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.*;
import one.stayfocused.backend.mapper.UserMapper;
import one.stayfocused.backend.model.*;
import one.stayfocused.backend.repository.*;
import one.stayfocused.backend.service.avatar.AvatarStorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request must not be null";

    private final AvatarStorageService avatarStorageService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = getUserByIdInternal(id);

        return  userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return  userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateName(Long id, UserNameUpdateRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        User user = getUserByIdInternal(id);
        user.setName(request.newName());

        return  userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto updateAvatar(Long id, UserAvatarUpdateRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user = getUserByIdInternal(id);

        user.setAvatarUrl(request.avatarUrl());
        return  userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto uploadAvatar(Long id, UserAvatarUploadRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user = getUserByIdInternal(id);

        String uploadedAvatarUrl = avatarStorageService.uploadAvatar(id, request.file());

        user.setAvatarUrl(uploadedAvatarUrl);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, UserRoleAssignmentRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        User user = getUserByIdInternal(userId);
        Role role = roleRepository.findByName(request.roleType())
                .orElseThrow(() -> new RoleNotFoundException(request.roleType()));

        if (!userRoleRepository.existsByUserAndRole(user, role)) {
            userRoleRepository.save(new UserRole(user, role));
        }
    }

    private User getUserByIdInternal(Long id) {
        return  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
