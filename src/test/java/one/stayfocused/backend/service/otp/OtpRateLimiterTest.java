package one.stayfocused.backend.service.otp;

import one.stayfocused.backend.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpRateLimiterTest {

    @Mock private OtpRepository otpRepository;

    private OtpRateLimiter otpRateLimiterService;

    private static final String OTP_TYPE = "otp-type";
    private static final String IDENTIFIER = "identifier";
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(10);
    private static final Duration REQUEST_LIMIT_DURATION = Duration.ofDays(1);

    @BeforeEach
    public void setUp() {
        otpRateLimiterService = new OtpRateLimitService(otpRepository);
    }

    @Test
    void testShouldReturnFalse_whenFailedAttemptsAreLessThanMaxAttempts() {
        doReturn(4)
                .when(otpRepository).getFailedAttempts(OTP_TYPE, IDENTIFIER);

        boolean result = otpRateLimiterService.isBlocked(OTP_TYPE, IDENTIFIER);

        assertFalse(result);
    }

    @Test
    void testShouldReturnTrue_whenFailedAttemptsReachedMaxAttempts() {
        doReturn(5)
                .when(otpRepository).getFailedAttempts(OTP_TYPE, IDENTIFIER);

        boolean result = otpRateLimiterService.isBlocked(OTP_TYPE, IDENTIFIER);

        assertTrue(result);
    }

    @Test
    void testShouldReturnFalse_whenRequestLimitIsNotExceeded() {
        doReturn(7)
                .when(otpRepository).getRequestCount(OTP_TYPE, IDENTIFIER);

        boolean result = otpRateLimiterService.isRequestLimitExceeded(OTP_TYPE, IDENTIFIER);

        assertFalse(result);
    }

    @Test
    void testShouldReturnFalse_whenRequestLimitExceeded() {
        doReturn(10)
                .when(otpRepository).getRequestCount(OTP_TYPE, IDENTIFIER);

        boolean result = otpRateLimiterService.isRequestLimitExceeded(OTP_TYPE, IDENTIFIER);

        assertTrue(result);
    }

    @Test
    void testShouldIncrementFailedAttempts() {
        doNothing()
                .when(otpRepository).incrementFailedAttempts(OTP_TYPE, IDENTIFIER, BLOCK_DURATION);

        otpRateLimiterService.incrementFailedAttempts(OTP_TYPE, IDENTIFIER);

        verify(otpRepository).incrementFailedAttempts(OTP_TYPE, IDENTIFIER, BLOCK_DURATION);
    }

    @Test
    void testShouldIncrementRequestCount() {
        doNothing()
                .when(otpRepository).incrementRequestCount(OTP_TYPE, IDENTIFIER, REQUEST_LIMIT_DURATION);

        otpRateLimiterService.incrementOtpRequestCount(OTP_TYPE, IDENTIFIER);

        verify(otpRepository).incrementRequestCount(OTP_TYPE, IDENTIFIER, REQUEST_LIMIT_DURATION);
    }
}
