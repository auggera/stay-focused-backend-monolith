package one.stayfocused.backend.mapper;

import one.stayfocused.backend.dto.UserRegisterResponseDto;
import one.stayfocused.backend.dto.UserResponseDto;
import one.stayfocused.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private User user;

    @BeforeEach
    void beforeAll() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@doe.com");
        user.setPassword("Password#123");
        user.setAvatarUrl("avatarUrl");
        user.setEmailVerified(false);
    }

    @Test
    void testToUserResponseDto() {
        UserResponseDto expected = new UserResponseDto(
                1L,
                "john@doe.com",
                "John Doe",
                "avatarUrl"
        );
        UserResponseDto actual = userMapper.toUserResponseDto(user);

        assertEquals(expected, actual);
    }

    @Test
    void testToRegisterResponseDto() {
        UserRegisterResponseDto expected = new UserRegisterResponseDto(
                "john@doe.com",
                "John Doe",
                false
        );
        UserRegisterResponseDto actual = userMapper.toRegisterResponseDto(user);

        assertEquals(expected, actual);
    }
}
