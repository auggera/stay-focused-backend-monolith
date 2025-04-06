package one.stayfocused.backend.service.user;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.config.AvatarConfig;
import one.stayfocused.backend.config.StorageConfig;
import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.*;
import one.stayfocused.backend.mapper.UserMapper;
import one.stayfocused.backend.model.*;
import one.stayfocused.backend.repository.*;
import one.stayfocused.backend.service.avatar.AvatarStorageFactory;
import one.stayfocused.backend.service.avatar.AvatarStorageService;
import one.stayfocused.backend.storage.StoragePathResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request must not be null";

    private final AvatarStorageFactory avatarStorageFactory;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final AvatarConfig avatarConfig;
    private final StorageConfig storageConfig;
    private final StoragePathResolver storagePathResolver;

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
        String currentAvatarUrl = user.getAvatarUrl();
        String newAvatarUrl = request.avatarUrl();

        if (shouldDeleteCurrentAvatar(currentAvatarUrl, newAvatarUrl)) {
            resolveAvatarStorageService(currentAvatarUrl)
                    .ifPresent(service -> service.deleteAvatar(currentAvatarUrl));
        }

        user.setAvatarUrl(newAvatarUrl);
        return  userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto uploadAvatar(Long id, UserAvatarUploadRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        String uploadedAvatarUrl = avatarStorageFactory.getService(storageConfig.getAvatar().getType())
                        .uploadAvatar(id, request.file());

        User user = getUserByIdInternal(id);
        String currentAvatarUrl = user.getAvatarUrl();

        if (shouldDeleteCurrentAvatar(currentAvatarUrl, uploadedAvatarUrl)) {
            resolveAvatarStorageService(currentAvatarUrl)
                    .ifPresent(service -> service.deleteAvatar(currentAvatarUrl));
        }

        user.setAvatarUrl(uploadedAvatarUrl);
        return  userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto deleteAvatar(Long id) {
        User user = getUserByIdInternal(id);
        String currentAvatarUrl = user.getAvatarUrl();

        if (avatarConfig.isDefaultAvatarUrl(currentAvatarUrl)) {
            throw new AvatarDeletionNotAllowedException();
        }

        resolveAvatarStorageService(currentAvatarUrl)
                .ifPresent(service -> service.deleteAvatar(currentAvatarUrl));

        user.setAvatarUrl(avatarConfig.assignRandomAvatar());
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserByIdInternal(id);

        String currentAvatarUrl = user.getAvatarUrl();

        if (!avatarConfig.isDefaultAvatarUrl(currentAvatarUrl)) {
            resolveAvatarStorageService(currentAvatarUrl)
                    .ifPresent(service -> service.deleteAvatar(currentAvatarUrl));
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

    private boolean shouldDeleteCurrentAvatar(String currentAvatarUrl, String newAvatarUrl) {
        return !Objects.equals(currentAvatarUrl, newAvatarUrl)
                && !avatarConfig.isDefaultAvatarUrl(currentAvatarUrl);
    }

    private Optional<AvatarStorageService> resolveAvatarStorageService(String avatarUrl) {
        return storagePathResolver.resolveStorageType(avatarUrl)
                .map(avatarStorageFactory::getService);
    }
}
