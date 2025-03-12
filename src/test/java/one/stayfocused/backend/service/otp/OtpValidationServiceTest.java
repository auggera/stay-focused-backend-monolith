package one.stayfocused.backend.service.otp;

import one.stayfocused.backend.exception.InvalidOtpException;
import one.stayfocused.backend.exception.OtpBlockedException;
import one.stayfocused.backend.exception.OtpNotFoundException;
import one.stayfocused.backend.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpValidationServiceTest {

    @Mock private  OtpRepository otpRepository;
    @Mock private  OtpRateLimiter otpRateLimiterService;

    OtpValidator otpValidationService;

    private static final String OTP_TYPE = "otp-type";
    private static final String IDENTIFIER = "identifier";
    private static final String OTP = "123456";

    @BeforeEach
    void setUp() {
        otpValidationService = new OtpValidationService(otpRepository, otpRateLimiterService);
    }

    @Test
    void shouldValidateOtp_whenOtpIsValid() {
        doReturn(false)
                .when(otpRateLimiterService).isBlocked(OTP_TYPE, IDENTIFIER);

        doReturn(OTP)
                .when(otpRepository).getOtp(OTP_TYPE, IDENTIFIER);

        otpValidationService.validateOtp(OTP_TYPE, IDENTIFIER, OTP);

        verify(otpRateLimiterService).isBlocked(OTP_TYPE, IDENTIFIER);
        verify(otpRepository).getOtp(OTP_TYPE, IDENTIFIER);
        verify(otpRepository).deleteOtp(OTP_TYPE, IDENTIFIER);
    }

    @Test
    void testShouldThrowOtpBlockedException_whenMaxFailedAttemptsExceeded() {
        doReturn(true)
                .when(otpRateLimiterService).isBlocked(OTP_TYPE, IDENTIFIER);

        assertThrows(OtpBlockedException.class,
                () -> otpValidationService.validateOtp(OTP_TYPE, IDENTIFIER, OTP)
        );
    }

    @Test
    void testShouldThrowOtpNotFoundException_whenOtpDoesNotExists() {
        doReturn(false)
                .when(otpRateLimiterService).isBlocked(OTP_TYPE, IDENTIFIER);

        doReturn(null)
                .when(otpRepository).getOtp(OTP_TYPE, IDENTIFIER);

        assertThrows(OtpNotFoundException.class,
                () ->  otpValidationService.validateOtp(OTP_TYPE, IDENTIFIER, OTP)
        );
    }

    @Test
    void testShouldThrowInvalidOtpException_whenOtpDoesNotMatchTheStoredOne() {
        doReturn(false)
                .when(otpRateLimiterService).isBlocked(OTP_TYPE, IDENTIFIER);

        doReturn("different-otp")
                .when(otpRepository).getOtp(OTP_TYPE, IDENTIFIER);

        assertThrows(InvalidOtpException.class,
                () -> otpValidationService.validateOtp(OTP_TYPE, IDENTIFIER, OTP)
        );
    }
}
