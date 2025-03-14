package one.stayfocused.backend.service.password;

import one.stayfocused.backend.dto.PasswordChangeWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordResetRequestDto;
import one.stayfocused.backend.dto.PasswordResetWithOtpRequestDto;
import one.stayfocused.backend.exception.SamePasswordException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import one.stayfocused.backend.service.EmailService;
import one.stayfocused.backend.service.otp.OtpGenerator;
import one.stayfocused.backend.service.otp.OtpValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PasswordOtpServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpGenerator otpGenerator;
    @Mock private OtpValidator otpValidator;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PasswordOtpServiceImpl passwordOtpService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "john@doe.com";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";
    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String NEW_VALID_PASSWORD = "newValidPassword#123";
    private static final String HASHED_PASSWORD = "#g4$Hf5dsd$dsd!ds";
    private static final String NEW_HASHED_PASSWORD = "$jd5fsJHj#ifdfs454";
    private static final String OTP = "123456";

    private static final PasswordChangeWithOtpRequestDto PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO = new PasswordChangeWithOtpRequestDto(OTP, NEW_VALID_PASSWORD);
    private static final PasswordResetRequestDto PASSWORD_RESET_REQUEST_DTO = new PasswordResetRequestDto(EMAIL);
    private static final PasswordResetWithOtpRequestDto PASSWORD_RESET_WITH_OTP_REQUEST_DTO = new PasswordResetWithOtpRequestDto(EMAIL, OTP, NEW_VALID_PASSWORD);

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setPassword(HASHED_PASSWORD);
    }

    @Test
    void testShouldSendOtp_whenRequestChangePasswordWithOtp() {
        mockUserFoundById();

        doReturn(OTP)
                .when(otpGenerator).generateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail());

        doNothing()
                .when(emailService).sendOtp(user.getEmail(), OTP);

        passwordOtpService.requestChangePasswordWithOtp(USER_ID);

        verify(otpGenerator).generateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail());
        verify(emailService).sendOtp(user.getEmail(), OTP);
    }

    @Test
    void testShouldThrowUserNotFoundException_whenRequestChangePasswordWithOtp_andUserDoesNotExists() {
        assertThrows(UserNotFoundException.class,
                () -> passwordOtpService.requestChangePasswordWithOtp(USER_ID)
        );
    }

    @Test
    void testShouldChangePasswordWithOtp_whenOtpIsValid() {
        mockUserFoundById();

        doNothing()
                .when(otpValidator).validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), OTP);

        mockPasswordMatching(false);

        System.out.println(user.getPassword());

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordOtpService.changePasswordWithOtp(USER_ID, PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO);

        verify(otpValidator).validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), OTP);
        verify(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
        verify(passwordEncoder).encode(NEW_VALID_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordChangeWithOtpRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordOtpService.changePasswordWithOtp(USER_ID, null)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordChangeWithOtp_andUserDoesNotExists() {
        mockUserNotFoundById();

        assertThrows(UserNotFoundException.class,
                () -> passwordOtpService.changePasswordWithOtp(USER_ID, PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordChangeWithOtp_andNewPasswordIsSame() {
        mockUserFoundById();

        doNothing()
                .when(otpValidator).validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), OTP);

        mockPasswordMatching(true);

        assertThrows(SamePasswordException.class,
                () -> passwordOtpService.changePasswordWithOtp(USER_ID, PASSWORD_CHANGE_WITH_OTP_REQUEST_DTO)
        );
    }

    @Test
    void testShouldSendOtp_whenPasswordResetRequestIsValid() {
        mockUserFoundByEmail();

        doReturn(OTP)
                .when(otpGenerator).generateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail());

        doNothing()
                .when(emailService).sendOtp(user.getEmail(), OTP);

        passwordOtpService.requestResetPasswordWithOtp(PASSWORD_RESET_REQUEST_DTO);

        verify(userRepository).findByEmail(PASSWORD_RESET_REQUEST_DTO.email());
        verify(otpGenerator).generateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail());
        verify(emailService).sendOtp(user.getEmail(), OTP);
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordResetRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordOtpService.requestResetPasswordWithOtp(null)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordResetRequest_andUserDoesNotExists() {
        mockUserNotFoundByEmail();

        assertThrows(UserNotFoundException.class,
                () -> passwordOtpService.requestResetPasswordWithOtp(PASSWORD_RESET_REQUEST_DTO)
        );
    }

    @Test
    void testShouldResetPassword_whenPasswordResetWithOtpRequestIsValid() {
        mockUserFoundByEmail();

        doNothing()
                .when(otpValidator).validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), OTP);

        mockPasswordMatching(false);

        doReturn(NEW_HASHED_PASSWORD)
                .when(passwordEncoder).encode(NEW_VALID_PASSWORD);

        passwordOtpService.resetPasswordWithOtp(PASSWORD_RESET_WITH_OTP_REQUEST_DTO);

        verify(userRepository).findByEmail(PASSWORD_RESET_WITH_OTP_REQUEST_DTO.email());
        verify(otpValidator).validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), OTP);
        verify(passwordEncoder).matches(NEW_VALID_PASSWORD, HASHED_PASSWORD);
        verify(passwordEncoder).encode(NEW_VALID_PASSWORD);
        verify(userRepository).save(user);
    }

    @Test
    void testShouldThrowNullPointerException_whenPasswordResetWithOtpRequestIsNull() {
        assertThrows(NullPointerException.class,
                () -> passwordOtpService.resetPasswordWithOtp(null)
        );
    }

    @Test
    void testShouldThrowUserNotFoundException_whenPasswordResetWithOtp_andUserDoesNotExists() {
        mockUserNotFoundByEmail();

        assertThrows(UserNotFoundException.class,
                () ->  passwordOtpService.resetPasswordWithOtp(PASSWORD_RESET_WITH_OTP_REQUEST_DTO)
        );
    }

    @Test
    void testShouldThrowSamePasswordException_whenPasswordResetWithOtp_andNewPasswordIsSame() {
        mockUserFoundByEmail();

        doNothing()
                .when(otpValidator).validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), OTP);

        mockPasswordMatching(true);

        assertThrows(SamePasswordException.class,
                () -> passwordOtpService.resetPasswordWithOtp(PASSWORD_RESET_WITH_OTP_REQUEST_DTO));
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
