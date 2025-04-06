package one.stayfocused.backend.service.user;

import one.stayfocused.backend.config.AvatarConfig;
import one.stayfocused.backend.config.StorageConfig;
import one.stayfocused.backend.dto.UserNameUpdateRequestDto;
import one.stayfocused.backend.dto.UserResponseDto;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.mapper.UserMapper;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.RoleRepository;
import one.stayfocused.backend.repository.UserRepository;
import one.stayfocused.backend.repository.UserRoleRepository;
import one.stayfocused.backend.service.avatar.AvatarStorageFactory;
import one.stayfocused.backend.storage.StoragePathResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private static final long USER_ID = 1L;
    private static final String USER_EMAIL = "john@doe.com";
    private UserResponseDto userDto;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userDto = new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                "John Doe",
                "avatarUrl"
        );

        mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setEmail(USER_EMAIL);
        mockUser.setName("John Doe");
        mockUser.setAvatarUrl("avatarUrl");
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

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(USER_ID));
    }

    @Test
    void testShouldThrowException_whenGetUserById_andIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> userService.getUserById(null));
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

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail(USER_EMAIL));
    }

    @Test
    void testShouldThrowException_whenGetUserByEmail_andEmailIsNull() {
        assertThrows(NullPointerException.class,
                () -> userService.getUserByEmail(null));
    }

    @Test
    void testShouldUpdateName_whenUpdateName_andRequestIsValid() {
        final String newName = "Jane White";
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto(newName);

        UserResponseDto expectedUserDto = new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                newName,
                "avatarUrl"
        );

        doReturn(Optional.of(mockUser))
                .when(userRepository).findById(USER_ID);

        doReturn(mockUser)
                .when(userRepository).save(mockUser);

        doReturn(expectedUserDto)
                .when(userMapper).toUserResponseDto(mockUser);

        UserResponseDto actualUserDto = userService.updateName(USER_ID, request);

        assertEquals(expectedUserDto, actualUserDto);
        verify(userRepository).findById(USER_ID);
        verify(userMapper).toUserResponseDto(mockUser);
        verify(userRepository).save(mockUser);
    }

    @Test
    void testShouldThrowException_whenUpdateName_andUserNotFound() {
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto("Jane White");

        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        assertThrows(UserNotFoundException.class,
                () -> userService.updateName(USER_ID, request));
    }

    @Test
    void testShouldThrowException_whenUpdateName_andUserIdIsNull() {
        UserNameUpdateRequestDto request = new UserNameUpdateRequestDto("Jane White");

        assertThrows(NullPointerException.class,
                () -> userService.updateName(null, request));
    }

    @Test
    void testShouldThrowException_whenUpdateName_andRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> userService.updateName(USER_ID, null));
    }
}
