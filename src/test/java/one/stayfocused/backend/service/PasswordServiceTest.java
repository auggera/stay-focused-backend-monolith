package one.stayfocused.backend.service;

import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.*;
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
    private static final String EMAIL = "john@doe.com";
    private static final String TOKEN_PASSWORD_CHANGE_KEY = "token:password-change:";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";
    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String VALID_PASSWORD = "validPassword#123";
    private static final String INVALID_PASSWORD = "invalidPassword";
    private static final String NEW_VALID_PASSWORD = "newValidPassword#123";
    private static final String HASHED_PASSWORD = "#g4$Hf5dsd$dsd!ds";
    private static final String NEW_HASHED_PASSWORD = "$jd5fsJHj#ifdfs454";
    private static final String OTP = "123456";
    private static final PasswordVerificationRequestDto VALID_PASSWORD_VERIFICATION_REQUEST_DTO = new PasswordVerificationRequestDto(VALID_PASSWORD);
    private static final PasswordVerificationRequestDto INVALID_PASSWORD_VERIFICATION_REQUEST_DTO = new PasswordVerificationRequestDto(INVALID_PASSWORD);
    private static final PasswordUpdateRequestDto PASSWORD_UPDATE_REQUEST_DTO = new PasswordUpdateRequestDto(NEW_VALID_PASSWORD);
    private static final PasswordChangeWithOtpRequestDto PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO = new PasswordChangeWithOtpRequestDto(OTP, NEW_VALID_PASSWORD);
    private static final PasswordResetRequestDto PASSWORD_RESET_REQUEST_DTO = new PasswordResetRequestDto(EMAIL);
    private static final PasswordResetWithOtpRequestDto PASSWORD_RESET_WITH_OTP_REQUEST_DTO = new PasswordResetWithOtpRequestDto(EMAIL, OTP, NEW_VALID_PASSWORD);

    private User user;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordServiceImpl(userRepository, otpService, emailService, passwordEncoder, redisTemplate);

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

        doReturn(false)
                .when(redisTemplate).delete(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doNothing()
                .when(valueOperations).set(startsWith(TOKEN_PASSWORD_CHANGE_KEY), anyString(), any(Duration.class));

        passwordService.verifyCurrentPassword(USER_ID, VALID_PASSWORD_VERIFICATION_REQUEST_DTO);

        verify(passwordEncoder).matches(VALID_PASSWORD_VERIFICATION_REQUEST_DTO.currentPassword(), user.getPassword());
        verify(redisTemplate).delete(startsWith(TOKEN_PASSWORD_CHANGE_KEY));
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(startsWith(TOKEN_PASSWORD_CHANGE_KEY), anyString(), any(Duration.class));
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

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn("mocked-token")
                .when(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        mockPasswordMatching(false);

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));
        verify(redisTemplate).delete(startsWith(TOKEN_PASSWORD_CHANGE_KEY));
        verify(userRepository).save(user);
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

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(null)
                .when(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        assertThrows(TokenNotFoundException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordUpdate_andNewPasswordIsSame() {
        mockUserFoundById();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn("mocked-token")
                .when(valueOperations).get(startsWith(TOKEN_PASSWORD_CHANGE_KEY));

        mockPasswordMatching(true);

        assertThrows(SamePasswordException.class,
                () -> passwordService.changePasswordAfterVerification(USER_ID, PASSWORD_UPDATE_REQUEST_DTO)
        );
    }

    @Test
    void testShouldSendOtp_whenRequestChangePasswordWithOtp() {
        mockUserFoundById();

        doReturn(OTP)
                .when(otpService).generateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail());

        doNothing()
                .when(emailService).sendOtp(user.getEmail(), OTP);

        passwordService.requestChangePasswordWithOtp(USER_ID);

        verify(otpService).generateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail());
        verify(emailService).sendOtp(user.getEmail(), OTP);
    }

    @Test
    void testShouldThrowUserNotFoundException_whenRequestChangePasswordWithOtp_andUserDoesNotExists() {
        assertThrows(UserNotFoundException.class,
                () -> passwordService.requestChangePasswordWithOtp(USER_ID)
        );
    }

    @Test
    void testShouldChangePasswordWithOtp_whenOtpIsValid() {
        mockUserFoundById();

        doNothing()
                .when(otpService).validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), OTP);

        mockPasswordMatching(false);

        System.out.println(user.getPassword());

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordService.changePasswordWithOtp(USER_ID, PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO);

        verify(otpService).validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), OTP);
        verify(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
        verify(passwordEncoder).encode(NEW_VALID_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordChangeWithOtpRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordService.changePasswordWithOtp(USER_ID, null)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordChangeWithOtp_andUserDoesNotExists() {
        mockUserNotFoundById();

        assertThrows(UserNotFoundException.class,
                () -> passwordService.changePasswordWithOtp(USER_ID, PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordChangeWithOtp_andNewPasswordIsSame() {
        mockUserFoundById();

        doNothing()
                .when(otpService).validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), OTP);

        mockPasswordMatching(true);

        assertThrows(SamePasswordException.class,
                () -> passwordService.changePasswordWithOtp(USER_ID, PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO)
        );
    }

    @Test
    void testShouldSendOtp_whenPasswordResetRequestIsValid() {
        mockUserFoundByEmail();

        doReturn(OTP)
                .when(otpService).generateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail());

        doNothing()
                .when(emailService).sendOtp(user.getEmail(), OTP);

        passwordService.requestResetPasswordWithOtp(PASSWORD_RESET_REQUEST_DTO);

        verify(userRepository).findByEmail(PASSWORD_RESET_REQUEST_DTO.email());
        verify(otpService).generateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail());
        verify(emailService).sendOtp(user.getEmail(), OTP);
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordResetRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordService.requestResetPasswordWithOtp(null)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordResetRequest_andUserDoesNotExists() {
        mockUserNotFoundByEmail();

        assertThrows(UserNotFoundException.class,
                () -> passwordService.requestResetPasswordWithOtp(PASSWORD_RESET_REQUEST_DTO)
        );
    }

    @Test
    void testShouldResetPassword_whenPasswordResetWithOtpRequestIsValid() {
        mockUserFoundByEmail();

        doNothing()
                .when(otpService).validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), OTP);

        mockPasswordMatching(false);

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordService.resetPasswordWithOtp(PASSWORD_RESET_WITH_OTP_REQUEST_DTO);

        verify(userRepository).findByEmail(PASSWORD_RESET_WITH_OTP_REQUEST_DTO.email());
        verify(otpService).validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), OTP);
        verify(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
        verify(passwordEncoder).encode(NEW_VALID_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordResetWithOtpRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordService.resetPasswordWithOtp(null)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordResetWithOtp_andUserDoesNotExists() {
        mockUserNotFoundByEmail();

        assertThrows(UserNotFoundException.class,
                () ->  passwordService.resetPasswordWithOtp(PASSWORD_RESET_WITH_OTP_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordResetWithOtp_andNewPasswordIsSame() {
        mockUserFoundByEmail();

        doNothing()
                .when(otpService).validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), OTP);

        mockPasswordMatching(true);

        assertThrows(SamePasswordException.class,
                () -> passwordService.resetPasswordWithOtp(PASSWORD_RESET_WITH_OTP_REQUEST_DTO));
    }

    private void mockUserFoundById() {
        doReturn(Optional.of(user))
                .when(userRepository).findById(USER_ID);
    }

    private void mockUserNotFoundById() {
        doReturn(Optional.empty())
                .when(userRepository).findById(USER_ID);
    }

    private void mockUserFoundByEmail() {
        doReturn(Optional.of(user))
                .when(userRepository).findByEmail(EMAIL);
    }

    private void mockUserNotFoundByEmail() {
        doReturn(Optional.empty())
                .when(userRepository).findByEmail(EMAIL);
    }

    private void mockPasswordMatching(boolean matches) {
        doReturn(matches)
                .when(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
    }
}