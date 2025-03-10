package one.stayfocused.backend.service;

import one.stayfocused.backend.dto.PasswordUpdateRequestDto;
import one.stayfocused.backend.dto.PasswordVerificationRequestDto;
import one.stayfocused.backend.exception.IncorrectCurrentPasswordException;
import one.stayfocused.backend.exception.SamePasswordException;
import one.stayfocused.backend.exception.TokenNotFoundException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpService otpService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private PasswordService passwordService;

    private static final Long USER_ID = 1L;
    private static final String TOKEN_PASSWORD_CHANGE_KEY = "token:password-change:";
    private static final String VALID_PASSWORD = "validPassword#123";
    private static final String INVALID_PASSWORD = "invalidPassword";
    private static final String NEW_VALID_PASSWORD = "newValidPassword#123";
    private static final String HASHED_PASSWORD = "#g4$Hf5dsd$dsd!ds";
    private static final String NEW_HASHED_PASSWORD = "$jd5fsJHj#ifdfs454";
    private static final PasswordVerificationRequestDto VALID_PASSWORD_VERIFICATION_REQUEST_DTO = new PasswordVerificationRequestDto(VALID_PASSWORD);
    private static final PasswordVerificationRequestDto INVALID_PASSWORD_VERIFICATION_REQUEST_DTO = new PasswordVerificationRequestDto(INVALID_PASSWORD);
    private static final PasswordUpdateRequestDto VALID_PASSWORD_UPDATE_REQUEST_DTO = new PasswordUpdateRequestDto(NEW_VALID_PASSWORD);

    private User user;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordServiceImpl(userRepository, otpService, emailService, passwordEncoder, redisTemplate);

        user = new User();
        user.setId(USER_ID);
        user.setPassword(HASHED_PASSWORD);

        lenient().doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

    }

    @Test
    void testShouldGenerateToken_whenCurrentPasswordIsCorrect() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);

        doReturn(true)
                .when(passwordEncoder).matches(VALID_PASSWORD, user.getPassword());

        passwordService.verifyCurrentPassword(USER_ID, VALID_PASSWORD_VERIFICATION_REQUEST_DTO);


        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(startsWith(TOKEN_PASSWORD_CHANGE_KEY), anyString(), any(Duration.class));
    }

    @Test
    void testShouldThrowIncorrectCurrentPasswordException_whenCurrentPasswordIsIncorrect() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);

        assertThrows(IncorrectCurrentPasswordException.class,
                () -> passwordService.verifyCurrentPassword(USER_ID, INVALID_PASSWORD_VERIFICATION_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenVerifyCurrentPassword_andUserDoesNotExists() {
        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);

        assertThrows(UserNotFoundException.class,
                () -> passwordService.verifyCurrentPassword(USER_ID, VALID_PASSWORD_VERIFICATION_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordVerificationRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordService.verifyCurrentPassword(USER_ID, null)
        );
    }

    @Test
    void testShouldChangePassword_whenTokenAndPasswordAreValid() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);

        doReturn("mocked-token")
                .when(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        doReturn(false)
                .when(passwordEncoder).matches(NEW_VALID_PASSWORD, user.getPassword());

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordService.changePasswordAfterVerification(USER_ID, VALID_PASSWORD_UPDATE_REQUEST_DTO);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));
        verify(redisTemplate).delete(startsWith(TOKEN_PASSWORD_CHANGE_KEY));
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordUpdate_andUserDoesNotExists() {
        doReturn(Optional.empty())
            .when(userRepository).findById(USER_ID);

        assertThrows(UserNotFoundException.class,
                ()  -> passwordService.changePasswordAfterVerification(USER_ID, VALID_PASSWORD_UPDATE_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordUpdateRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, null)
        );
    }

    @Test
    void testShouldThrowTokenNotFoundException_whenPasswordUpdate_andTokenDoesNotExists() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);

        doReturn(null)
                .when(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        assertThrows(TokenNotFoundException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, VALID_PASSWORD_UPDATE_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordUpdate_andNewPasswordIsSame() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);

        doReturn("mocked-token")
                .when(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        doReturn(true)
                .when(passwordEncoder).matches(NEW_VALID_PASSWORD, user.getPassword());

        assertThrows(SamePasswordException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, VALID_PASSWORD_UPDATE_REQUEST_DTO)
        );
    }
}
