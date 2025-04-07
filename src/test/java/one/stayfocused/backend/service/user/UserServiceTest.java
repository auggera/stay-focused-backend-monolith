package one.stayfocused.backend.service.user;

import one.stayfocused.backend.config.AvatarConfig;
import one.stayfocused.backend.config.StorageConfig;
import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.AvatarDeletionNotAllowedException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.mapper.UserMapper;
import one.stayfocused.backend.model.Role;
import one.stayfocused.backend.model.RoleType;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.model.UserRole;
import one.stayfocused.backend.repository.RoleRepository;
import one.stayfocused.backend.repository.UserRepository;
import one.stayfocused.backend.repository.UserRoleRepository;
import one.stayfocused.backend.service.avatar.AvatarStorageFactory;
import one.stayfocused.backend.service.avatar.AvatarStorageService;
import one.stayfocused.backend.service.avatar.CloudinaryAvatarStorageService;
import one.stayfocused.backend.storage.StoragePathResolver;
import one.stayfocused.backend.storage.StorageType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private AvatarStorageFactory avatarStorageFactory;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private UserMapper userMapper;
    @Mock private AvatarConfig avatarConfig;
    @Mock private StorageConfig storageConfig;
    @Mock private StoragePathResolver storagePathResolver;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request must not be null";
    private static final String USER_ID_NULL_ERROR_MESSAGE = "User ID must not be null";
    private static final String USER_EMAIL_NULL_ERROR_MESSAGE = "User's email must not be null";

    private static final long USER_ID = 1L;
    private static final String USER_EMAIL = "john@doe.com";
    private static final String NEW_AVATAR_URL = "newAvatarUrl";
    private static final String CURRENT_AVATAR_URL = "currentAvatarUrl";

    private final AvatarStorageService mockStorageService = mock(CloudinaryAvatarStorageService.class);
    private final StorageConfig.ResourceStorage mockResourceStorage = mock(StorageConfig.ResourceStorage.class);

    private UserResponseDto userDto;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userDto = new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                "John Doe",
                CURRENT_AVATAR_URL
        );

        mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setEmail(USER_EMAIL);
        mockUser.setName("John Doe");
        mockUser.setAvatarUrl(CURRENT_AVATAR_URL);
    }

    @Test
    void testShouldReturnUserResponseDto_whenGetUserById() {
        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(userDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto receivedUserDto = userService.getUserById(USER_ID);

        assertEquals(userDto, receivedUserDto);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldThrowException_whenGetUserById_andUserNotFound() {
        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(USER_ID));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
    }

    @Test
    void testShouldThrowException_whenGetUserById_andIdIsNull() {
        Exception ex =assertThrows(NullPointerException.class,
                () -> userService.getUserById(null));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void testShouldReturnUserResponseDto_whenGetUserByEmail() {
        doReturn(Optional.of(mockUser))
                .when(userRepository).findByEmail(USER_EMAIL);

        doReturn(userDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto receivedUserDto = userService.getUserByEmail(USER_EMAIL);

        assertEquals(userDto, receivedUserDto);
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldThrowException_whenGetUserByEmail_andUserNotFound() {
        doReturn(Optional.empty())
                .when(userRepository).findByEmail(USER_EMAIL);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail(USER_EMAIL));

        assertEquals("User not found with email: " + USER_EMAIL, ex.getMessage());
    }

    @Test
    void testShouldThrowException_whenGetUserByEmail_andEmailIsNull() {
        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.getUserByEmail(null));

        assertEquals(USER_EMAIL_NULL_ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void testShouldUpdateName_whenUpdateName_andRequestIsValid() {
        final String newName = "Jane White";
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto(newName);

        UserResponseDto expectedUserDto = initializeUserDtoWithName(newName);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(mockUser)
                .when(userRepository).save(mockUser);

        doReturn(expectedUserDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto actualUserDto = userService.updateName(USER_ID, request);

        assertEquals(expectedUserDto, actualUserDto);
        assertEquals(newName, actualUserDto.name());
        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(mockUser);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldThrowException_whenUpdateName_andUserNotFound() {
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto("Jane White");

        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.updateName(USER_ID, request));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(mockUser);
    }

    @Test
    void testShouldThrowException_whenUpdateName_andUserIdIsNull() {
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto("Jane White");

        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.updateName(null, request));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(USER_ID);
        verify(userRepository, never()).save(mockUser);
    }

    @Test
    void testShouldThrowException_whenUpdateName_andRequestIsNull() {
        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.updateName(USER_ID, null));

        assertEquals(REQUEST_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(USER_ID);
        verify(userRepository, never()).save(mockUser);
    }

    @Test
    void testShouldUpdateAvatar_whenUpdateAvatar_andRequestIsValid() {
        UserAvatarUpdateRequestDto request = new UserAvatarUpdateRequestDto(NEW_AVATAR_URL);
        UserResponseDto expectedUserDto = initializeUserDtoWithAvatarUrl(NEW_AVATAR_URL);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(true)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        doReturn(mockUser)
                .when(userRepository).save(mockUser);

        doReturn(expectedUserDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto actualUserDto = userService.updateAvatar(USER_ID, request);

        assertEquals(expectedUserDto, actualUserDto);
        assertEquals(NEW_AVATAR_URL, actualUserDto.avatarUrl());
        verify(userRepository).findById(USER_ID);
        verify(storagePathResolver, never()).resolveStorageType(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory, never()).getService(any());
        verify(userRepository).save(mockUser);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldUpdateAvatar_andDeleteCurrentAvatar_whenRequestIsValid() {
        UserAvatarUpdateRequestDto request = new UserAvatarUpdateRequestDto(NEW_AVATAR_URL);
        UserResponseDto expectedUserDto = initializeUserDtoWithAvatarUrl(NEW_AVATAR_URL);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(false)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        doReturn(Optional.of(StorageType.CLOUDINARY))
                .when(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);

        doReturn(mockStorageService)
                .when(avatarStorageFactory).getService(StorageType.CLOUDINARY);

        doReturn(mockUser)
                .when(userRepository).save(mockUser);

        doReturn(expectedUserDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto actualUserDto = userService.updateAvatar(USER_ID, request);

        assertEquals(expectedUserDto, actualUserDto);
        assertEquals(NEW_AVATAR_URL, actualUserDto.avatarUrl());
        verify(userRepository).findById(USER_ID);
        verify(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory).getService(StorageType.CLOUDINARY);
        verify(mockStorageService).deleteAvatar(CURRENT_AVATAR_URL);
        verify(userRepository).save(mockUser);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldThrowException_whenUpdateAvatar_andUserNotFound() {
        UserAvatarUpdateRequestDto request = new UserAvatarUpdateRequestDto(NEW_AVATAR_URL);

        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.updateAvatar(USER_ID, request));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(storagePathResolver, never()).resolveStorageType(any());
        verify(avatarStorageFactory, never()).getService(any());
        verify(userRepository, never()).save(mockUser);
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void testShouldThrowException_whenUpdateAvatar_andRequestIsNull() {
        Exception ex =assertThrows(NullPointerException.class,
                () -> userService.updateAvatar(USER_ID, null));

        assertEquals(REQUEST_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(USER_ID);
        verify(storagePathResolver, never()).resolveStorageType(any());
        verify(avatarStorageFactory, never()).getService(any());
        verify(userRepository, never()).save(mockUser);
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void testShouldThrowException_whenUpdateAvatar_andUserIdIsNull() {
        UserAvatarUpdateRequestDto request = new UserAvatarUpdateRequestDto(NEW_AVATAR_URL);

        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.updateAvatar(null, request));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(USER_ID);
        verify(storagePathResolver, never()).resolveStorageType(any());
        verify(avatarStorageFactory, never()).getService(any());
        verify(userRepository, never()).save(mockUser);
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void testShouldUploadAvatar_whenUploadAvatar_andRequestIsValid() {
        MultipartFile file = initializeMockMultipartFile();
        UserAvatarUploadRequestDto request = new UserAvatarUploadRequestDto(file);

        UserResponseDto expectedUserDto = initializeUserDtoWithAvatarUrl(NEW_AVATAR_URL);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(mockResourceStorage)
                .when(storageConfig).getAvatar();

        doReturn(StorageType.CLOUDINARY)
                .when(mockResourceStorage).getType();

        doReturn(mockStorageService)
                .when(avatarStorageFactory).getService(StorageType.CLOUDINARY);

        doReturn(NEW_AVATAR_URL)
                .when(mockStorageService).uploadAvatar(USER_ID, file);

        doReturn(true)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        doReturn(mockUser)
                .when(userRepository).save(mockUser);

        doReturn(expectedUserDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto actualUserDto = userService.uploadAvatar(USER_ID, request);

        assertEquals(expectedUserDto, actualUserDto);
        assertEquals(NEW_AVATAR_URL, actualUserDto.avatarUrl());
        verify(userRepository).findById(USER_ID);
        verify(avatarStorageFactory).getService(StorageType.CLOUDINARY);
        verify(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);
        verifyNoMoreInteractions(avatarStorageFactory);
        verify(mockStorageService, never()).deleteAvatar(CURRENT_AVATAR_URL);
        verify(userRepository).save(mockUser);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldUploadAvatar_andDeleteCurrentAvatar_whenRequestIsValid() {
        MultipartFile file = initializeMockMultipartFile();
        UserAvatarUploadRequestDto request = new UserAvatarUploadRequestDto(file);

        UserResponseDto expectedUserDto = initializeUserDtoWithAvatarUrl(NEW_AVATAR_URL);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(mockResourceStorage)
                .when(storageConfig).getAvatar();

        doReturn(StorageType.CLOUDINARY)
                .when(mockResourceStorage).getType();

        doReturn(NEW_AVATAR_URL)
                .when(mockStorageService).uploadAvatar(USER_ID, file);

        doReturn(false)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        doReturn(Optional.of(StorageType.CLOUDINARY))
                .when(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);

        doReturn(mockStorageService)
                .when(avatarStorageFactory).getService(StorageType.CLOUDINARY);

        doReturn(mockUser)
                .when(userRepository).save(mockUser);

        doReturn(expectedUserDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto actualUserDto = userService.uploadAvatar(USER_ID, request);

        assertEquals(expectedUserDto, actualUserDto);
        assertEquals(NEW_AVATAR_URL, actualUserDto.avatarUrl());
        verify(userRepository).findById(USER_ID);
        verify(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory, times(2)).getService(StorageType.CLOUDINARY);
        verify(mockStorageService).deleteAvatar(CURRENT_AVATAR_URL);
        verify(userRepository).save(mockUser);
        verify(userMapper).toUserResponseDto(mockUser);
    }

    @Test
    void testShouldThrowException_whenUploadAvatar_andUserNotFound() {
        MultipartFile file = initializeMockMultipartFile();
        UserAvatarUploadRequestDto request = new UserAvatarUploadRequestDto(file);

        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.uploadAvatar(USER_ID, request));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(userRepository, never()).save(mockUser);
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void testShouldThrowException_whenUploadAvatar_andUserIdIsNull() {
        MultipartFile file = initializeMockMultipartFile();
        UserAvatarUploadRequestDto request = new UserAvatarUploadRequestDto(file);

        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.uploadAvatar(null, request));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(any());
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(userRepository, never()).save(mockUser);
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void testShouldThrowException_whenUploadAvatar_andRequestIsNull() {
        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.uploadAvatar(USER_ID, null));

        assertEquals(REQUEST_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(any());
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(userRepository, never()).save(mockUser);
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void testShouldDeleteAvatar_whenDeleteAvatar() {
        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(false)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        doReturn(Optional.of(StorageType.CLOUDINARY))
                .when(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);

        doReturn(mockStorageService)
                .when(avatarStorageFactory).getService(StorageType.CLOUDINARY);

        userService.deleteAvatar(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);
        verify(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory).getService(StorageType.CLOUDINARY);
        verify(mockStorageService).deleteAvatar(CURRENT_AVATAR_URL);
        verify(userRepository).save(mockUser);
    }

    @Test
    void testShouldThrowException_whenDeleteAvatar_andCurrentAvatarIsDefault() {
        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(true)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        Exception ex = assertThrows(AvatarDeletionNotAllowedException.class,
                () -> userService.deleteAvatar(USER_ID));

        assertEquals("Cannot delete default avatar", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);
        verify(storagePathResolver, never()).resolveStorageType(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(userRepository, never()).save(mockUser);

    }

    @Test
    void testShouldThrowException_whenDeleteAvatar_andUserNotFound() {
        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.deleteAvatar(USER_ID));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(storagePathResolver, never()).resolveStorageType(anyString());
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(userRepository, never()).save(mockUser);
    }

    @Test
    void testShouldThrowException_whenDeleteAvatar_andUserIdIsNull() {
        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.deleteAvatar(null));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(storagePathResolver, never()).resolveStorageType(anyString());
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(userRepository, never()).save(mockUser);
    }

    @Test
    void testShouldDeleteUser_andDeleteCurrentAvatar_whenDeleteUser() {
        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(false)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        doReturn(Optional.of(StorageType.CLOUDINARY))
                .when(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);

        doReturn(mockStorageService)
                .when(avatarStorageFactory).getService(StorageType.CLOUDINARY);

        userService.deleteUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);
        verify(storagePathResolver).resolveStorageType(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory).getService(StorageType.CLOUDINARY);
        verify(mockStorageService).deleteAvatar(CURRENT_AVATAR_URL);
        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void testShouldDeleteUser_whenDeleteUser_andCurrentAvatarIsDefault() {
        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(true)
                .when(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);

        userService.deleteUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(avatarConfig).isDefaultAvatarUrl(CURRENT_AVATAR_URL);
        verify(storagePathResolver, never()).resolveStorageType(CURRENT_AVATAR_URL);
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void testShouldThrowException_whenDeleteUser_andUserNotFound() {
        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(USER_ID));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(storagePathResolver, never()).resolveStorageType(anyString());
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(userRepository, never()).deleteById(USER_ID);
    }

    @Test
    void testShouldThrowException_whenDeleteUser_andUserIdIsNull() {
        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.deleteUser(null));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(avatarConfig, never()).isDefaultAvatarUrl(anyString());
        verify(storagePathResolver, never()).resolveStorageType(anyString());
        verify(avatarStorageFactory, never()).getService(StorageType.CLOUDINARY);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testShouldAssignRole_whenAssignRole_andRequestIsValid() {
        UserRoleAssignmentRequestDto request = new UserRoleAssignmentRequestDto(RoleType.USER);
        Role mockRole = initializeMockRole(RoleType.USER);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(Optional.of(mockRole))
                .when(roleRepository).findByName(RoleType.USER);

        doReturn(false)
                .when(userRoleRepository).existsByUserAndRole(mockUser, mockRole);

        userService.assignRole(USER_ID, request);

        verify(userRepository).findById(USER_ID);
        verify(roleRepository).findByName(RoleType.USER);
        verify(userRoleRepository).existsByUserAndRole(mockUser, mockRole);
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void testShouldNotAssignRole_whenRoleIsAlreadyAssigned() {
        UserRoleAssignmentRequestDto request = new UserRoleAssignmentRequestDto(RoleType.ADMIN);
        Role mockRole = initializeMockRole(RoleType.ADMIN);

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(Optional.of(mockRole))
                .when(roleRepository).findByName(RoleType.ADMIN);

        doReturn(true)
                .when(userRoleRepository).existsByUserAndRole(mockUser, mockRole);

        userService.assignRole(USER_ID, request);

        verify(userRepository).findById(USER_ID);
        verify(roleRepository).findByName(RoleType.ADMIN);
        verify(userRoleRepository).existsByUserAndRole(mockUser, mockRole);
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void testShouldThrowException_whenAssignRole_andUserNotFound() {
        UserRoleAssignmentRequestDto request = new UserRoleAssignmentRequestDto(RoleType.USER);
        Role mockRole = initializeMockRole(RoleType.USER);

        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        Exception ex = assertThrows(UserNotFoundException.class,
                () -> userService.assignRole(USER_ID, request));

        assertEquals("User not found with ID: " + USER_ID, ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(roleRepository, never()).findByName(RoleType.USER);
        verify(userRoleRepository, never()).existsByUserAndRole(mockUser, mockRole);
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void testShouldThrowException_whenAssignRole_andUserIdIsNull() {
        UserRoleAssignmentRequestDto request = new UserRoleAssignmentRequestDto(RoleType.USER);
        Role mockRole = initializeMockRole(RoleType.USER);

        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.assignRole(null, request));

        assertEquals(USER_ID_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(roleRepository, never()).findByName(RoleType.USER);
        verify(userRoleRepository, never()).existsByUserAndRole(mockUser, mockRole);
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void testShouldThrowException_whenAssignRole_andRequestIsNull() {
        Exception ex = assertThrows(NullPointerException.class,
                () -> userService.assignRole(USER_ID, null));

        assertEquals(REQUEST_NULL_ERROR_MESSAGE, ex.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(roleRepository, never()).findByName(any(RoleType.class));
        verify(userRoleRepository, never()).existsByUserAndRole(any(User.class), any(Role.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    private static MultipartFile initializeMockMultipartFile() {
        byte[] fileContent =  new byte[10];
        final String fileName = "image.jpg";
        return new MockMultipartFile(fileName, fileContent);
    }

    private static Role initializeMockRole(RoleType roleType) {
        Role mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setName(roleType);
        return mockRole;
    }

    private static UserResponseDto initializeUserDtoWithName(String name) {
        return new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                name,
                CURRENT_AVATAR_URL
        );
    }

    private static UserResponseDto initializeUserDtoWithAvatarUrl(String avatarUrl) {
        return new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                "John Doe",
                avatarUrl
        );
    }
}
