package one.stayfocused.backend.service.password;

import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.*;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.EphemeralTokenRepository;
import one.stayfocused.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private EphemeralTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PasswordServiceImpl passwordService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "john@doe.com";
    private static final String TOKEN_TYPE_PASSWORD_CHANGE = "password-change";
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(5);
    private static final String VALID_PASSWORD = "validPassword#123";
    private static final String INVALID_PASSWORD = "invalidPassword";
    private static final String NEW_VALID_PASSWORD = "newValidPassword#123";
    private static final String HASHED_PASSWORD = "#g4$Hf5dsd$dsd!ds";
    private static final String NEW_HASHED_PASSWORD = "$jd5fsJHj#ifdfs454";
    private static final PasswordVerificationRequestDto VALID_PASSWORD_VERIFICATION_REQUEST_DTO = new PasswordVerificationRequestDto(VALID_PASSWORD);
    private static final PasswordVerificationRequestDto INVALID_PASSWORD_VERIFICATION_REQUEST_DTO = new PasswordVerificationRequestDto(INVALID_PASSWORD);
    private static final PasswordUpdateRequestDto PASSWORD_UPDATE_REQUEST_DTO = new PasswordUpdateRequestDto(NEW_VALID_PASSWORD);

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setPassword(HASHED_PASSWORD);
    }

    @Test
    void testShouldGenerateToken_whenCurrentPasswordIsCorrect() {
        mockUserFoundById();

        doReturn(true)
                .when(passwordEncoder).matches(VALID_PASSWORD, user.getPassword());

        doNothing()
                .when(tokenRepository).deleteToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());

        doNothing()
                .when(tokenRepository).saveToken(eq(TOKEN_TYPE_PASSWORD_CHANGE), eq(user.getEmail()), anyString(), eq(TOKEN_EXPIRATION));

        passwordService.verifyCurrentPassword(USER_ID, VALID_PASSWORD_VERIFICATION_REQUEST_DTO);

        verify(passwordEncoder).matches(VALID_PASSWORD_VERIFICATION_REQUEST_DTO.currentPassword(), user.getPassword());
        verify(tokenRepository).deleteToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());
        verify(tokenRepository).saveToken(eq(TOKEN_TYPE_PASSWORD_CHANGE), eq(user.getEmail()), anyString(), eq(TOKEN_EXPIRATION));
    }

    @Test
    void testShouldThrowIncorrectCurrentPasswordException_whenCurrentPasswordIsIncorrect() {
        mockUserFoundById();

        assertThrows(IncorrectCurrentPasswordException.class,
                () -> passwordService.verifyCurrentPassword(USER_ID, INVALID_PASSWORD_VERIFICATION_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenVerifyCurrentPassword_andUserDoesNotExists() {
        mockUserNotFoundById();

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
        mockUserFoundById();

        doReturn("mocked-token")
                .when(tokenRepository).getToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());

        mockPasswordMatching(false);

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO);

        verify(tokenRepository).getToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());
        verify(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
        verify(passwordEncoder).encode(NEW_VALID_PASSWORD);
        verify(userRepository).save(user);
        verify(tokenRepository).deleteToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordUpdate_andUserDoesNotExists() {
        mockUserNotFoundById();

        assertThrows(UserNotFoundException.class,
                ()  -> passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO)
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
        mockUserFoundById();

        doReturn(null)
                .when(tokenRepository).getToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());

        assertThrows(TokenNotFoundException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordUpdate_andNewPasswordIsSame() {
        mockUserFoundById();

        doReturn("mocked-token")
                .when(tokenRepository).getToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());

        mockPasswordMatching(true);

        assertThrows(SamePasswordException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO)
        );
    }

    private void mockUserFoundById() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);
    }

    private void mockUserNotFoundById() {
        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);
    }

    private void mockPasswordMatching(boolean matches) {
        doReturn(matches)
                .when(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
    }
}