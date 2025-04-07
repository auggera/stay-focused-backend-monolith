package one.stayfocused.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisOtpRepositoryTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    private OtpRepository redisOtpRepository;

    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPT_PREFIX = "attempts:";
    private static final String REQUEST_LIMIT_PREFIX = "otp-request:";
    private static final String OTP_TYPE = "otp-type";
    private static final String IDENTIFIER = "identifier";
    private static final String OTP = "123456";
    private static final Duration EXPIRATION = Duration.ofMinutes(5);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(10);
    private static final Duration REQUEST_LIMIT_DURATION = Duration.ofDays(1);
    private static final long MAX_ATTEMPTS = 5;

    @BeforeEach
    void setUp() {
        redisOtpRepository = new RedisOtpRepository(redisTemplate);
    }

    @Test
    void testShouldSaveOtp_whenSaveOtp() {
        final String otpKey = buildOtpKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doNothing()
                .when(valueOperations).set(otpKey, OTP, EXPIRATION);

        redisOtpRepository.saveOtp(OTP_TYPE, IDENTIFIER, OTP, EXPIRATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(otpKey, OTP, EXPIRATION);
    }

    @Test
    void testShouldReturnOtp_whenGetOtp() {
        final String otpKey = buildOtpKey();
        final String expected = OTP;

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(expected)
                .when(valueOperations).get(otpKey);

        final String actual = redisOtpRepository.getOtp(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(otpKey);

        assertEquals(expected, actual);
    }

    @Test
    void testShouldReturnNull_whenGetOtp_andOtpNotFound() {
        final String otpKey = buildOtpKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        final String storedOtp = redisOtpRepository.getOtp(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(otpKey);

        assertNull(storedOtp);
    }

    @Test
    void testShouldDeleteOtp_whenDeleteOtp() {
        final String otpKey = buildOtpKey();

        doReturn(true)
                .when(redisTemplate).delete(otpKey);

        redisOtpRepository.deleteOtp(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).delete(otpKey);
    }

    @Test
    void testShouldReturnFourFailedAttempts_whenGetFailedAttempts() {
        final String attemptKey = buildAttemptKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn("4")
                .when(valueOperations).get(attemptKey);

        int actual = redisOtpRepository.getFailedAttempts(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(attemptKey);

        assertEquals(Integer.parseInt("4"), actual);
    }

    @Test
    void testShouldReturnZero_whenGetFailedAttempts_andAttemptsAreNull(){
        final String attemptKey = buildAttemptKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(null)
                .when(valueOperations).get(attemptKey);

        int actual = redisOtpRepository.getFailedAttempts(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(attemptKey);

        assertEquals(0, actual);
    }

    @Test
    void testShouldIncrementFailedAttempts_whenFailedAttempt() {
        String attemptKey = buildAttemptKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(4L)
                .when(valueOperations).increment(attemptKey);

        redisOtpRepository.incrementFailedAttempts(OTP_TYPE, IDENTIFIER, BLOCK_DURATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).increment(attemptKey);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void testShouldIncrementFailedAttempts_whenFirstAttemptOrAttemptCountIsNull() {
        String attemptKey = buildAttemptKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(null)
                .when(valueOperations).increment(attemptKey);

        redisOtpRepository.incrementFailedAttempts(OTP_TYPE, IDENTIFIER, BLOCK_DURATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).increment(attemptKey);
        verify(redisTemplate).expire(attemptKey, BLOCK_DURATION);
    }

    @Test
    void testShouldImposeABlockDuration_whenReachedMaxAttempts() {
        String attemptKey = buildAttemptKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(MAX_ATTEMPTS)
                .when(valueOperations).increment(attemptKey);

        redisOtpRepository.incrementFailedAttempts(OTP_TYPE, IDENTIFIER, BLOCK_DURATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).increment(attemptKey);
        verify(redisTemplate).expire(attemptKey, BLOCK_DURATION);
    }

    @Test
    void testShouldReturnRequestCount_whenGetRequestCount() {
        final String requestKey = buildRequestLimitKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn("2")
            .when(valueOperations).get(requestKey);

        int actual = redisOtpRepository.getRequestCount(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(requestKey);

        assertEquals(2, actual);
    }

    @Test
    void testShouldReturnZero_whenGetRequestCount_andRequestCountIsNull () {
        final String requestKey = buildRequestLimitKey();

        doReturn(valueOperations)
            .when(redisTemplate).opsForValue();

        doReturn(null)
                .when(valueOperations).get(requestKey);

        int actual = redisOtpRepository.getRequestCount(OTP_TYPE, IDENTIFIER);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(requestKey);

        assertEquals(0, actual);
    }

    @Test
    void testShouldIncrementRequestCount_whenGetRequestCount() {
        String requestKey = buildRequestLimitKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(4L)
                .when(valueOperations).increment(requestKey);

        redisOtpRepository.incrementRequestCount(OTP_TYPE, IDENTIFIER, REQUEST_LIMIT_DURATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).increment(requestKey);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void testShouldImposeABlockDuration_whenFirstRequestOrRequestCountIsNull() {
        String attemptKey = buildRequestLimitKey();

        doReturn(valueOperations)
                .when(redisTemplate).opsForValue();

        doReturn(null)
                .when(valueOperations).increment(attemptKey);

        redisOtpRepository.incrementRequestCount(OTP_TYPE, IDENTIFIER, REQUEST_LIMIT_DURATION);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).increment(attemptKey);
        verify(redisTemplate).expire(attemptKey, REQUEST_LIMIT_DURATION);
    }

    @ParameterizedTest
    @MethodSource(value = "validOtpTypeProvider")
    void testShouldReturnFiveMinutes_whenOtpTypeIsValid(String otpType) {
        Duration duration = redisOtpRepository.getOtpExpiration(otpType);
        assertEquals(5, duration.toMinutes());
    }

    private static Stream<Arguments> validOtpTypeProvider() {
        return Stream.of(
                Arguments.of("password-change"),
                Arguments.of("password-reset"),
                Arguments.of("email-confirm")
        );
    }

    @Test
    void testShouldReturnDefaultDuration_whenOtpTypeIsInvalid() {
        Duration duration = redisOtpRepository.getOtpExpiration("invalid-otp-type");
        assertEquals(Duration.ofMinutes(2), duration);
    }

    private String buildOtpKey() {
        return OTP_PREFIX + OTP_TYPE + ":" + IDENTIFIER;
    }

    private String buildAttemptKey() {
        return ATTEMPT_PREFIX + OTP_TYPE + ":" + IDENTIFIER;
    }

    private String buildRequestLimitKey() {
        return REQUEST_LIMIT_PREFIX + OTP_TYPE + ":" + IDENTIFIER;
    }
}
