package one.stayfocused.backend.service.otp;

import one.stayfocused.backend.exception.OtpRequestLimitExceededException;
import one.stayfocused.backend.repository.OtpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpGeneratorServiceTest {

    @Mock private OtpRepository otpRepository;
    @Mock private OtpRateLimiter otpRateLimiterService;
    @InjectMocks private OtpGeneratorService otpGeneratorService;

    private static final String OTP_TYPE = "otp-type";
    private static final String IDENTIFIER = "identifier";
    private static final Duration EXPIRATION = Duration.ofMinutes(5);

    @Test
    void testShouldGenerateOtp_whenRequestLimitNotExceeded() {

        doReturn(false)
                .when(otpRateLimiterService).isRequestLimitExceeded(OTP_TYPE, IDENTIFIER);

        doReturn(EXPIRATION)
                .when(otpRepository).getOtpExpiration(OTP_TYPE);

        doNothing()
                .when(otpRepository).saveOtp(eq(OTP_TYPE), eq(IDENTIFIER), anyString(), eq(EXPIRATION));

        doNothing()
                .when(otpRateLimiterService).incrementOtpRequestCount(OTP_TYPE, IDENTIFIER);

        String generatedOtp = otpGeneratorService.generateOtp(OTP_TYPE, IDENTIFIER);

        verify(otpRateLimiterService).isRequestLimitExceeded(OTP_TYPE, IDENTIFIER);
        verify(otpRepository).deleteOtp(OTP_TYPE, IDENTIFIER);
        verify(otpRepository).getOtpExpiration(OTP_TYPE);
        verify(otpRepository).saveOtp(eq(OTP_TYPE), eq(IDENTIFIER), anyString(), eq(EXPIRATION));
        verify(otpRateLimiterService).incrementOtpRequestCount(OTP_TYPE, IDENTIFIER);

        assertNotNull(generatedOtp);
        assertEquals(6, generatedOtp.length() );
    }

    @Test
    void testShouldThrowOtpRequestLimitExceededException_whenRequestLimitExceeded() {
        doReturn(true)
                .when(otpRateLimiterService).isRequestLimitExceeded(OTP_TYPE, IDENTIFIER);

        assertThrows(OtpRequestLimitExceededException.class,
                () -> otpGeneratorService.generateOtp(OTP_TYPE, IDENTIFIER)
        );
    }


}
