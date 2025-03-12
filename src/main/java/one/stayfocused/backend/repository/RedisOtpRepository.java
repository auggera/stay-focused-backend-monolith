package one.stayfocused.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Objects;

@RequiredArgsConstructor
@Repository
public class RedisOtpRepository implements OtpRepository {

    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPT_PREFIX = "attempts:";
    private static final String REQUEST_LIMIT_PREFIX = "otp-request:";
    private static final int MAX_ATTEMPTS = 5;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveOtp(String otpType, String identifier, String otp, Duration expiration) {
        redisTemplate.opsForValue().set(buildOtpKey(otpType, identifier), otp, expiration);
    }

    @Override
    public String getOtp(String otpType, String identifier) {
        return  redisTemplate.opsForValue().get(buildOtpKey(otpType, identifier));
    }

    @Override
    public void deleteOtp(String otpType, String identifier) {
        redisTemplate.delete(buildOtpKey(otpType, identifier));
    }

    @Override
    public int getFailedAttempts(String otpType, String identifier) {
        String count = redisTemplate.opsForValue().get(buildAttemptKey(otpType, identifier));
        return count != null ? Integer.parseInt(count) : 0;
    }

    @Override
    public void incrementFailedAttempts(String otpType, String identifier, Duration blockDuration) {
        Long attemptCount = redisTemplate.opsForValue().increment(buildAttemptKey(otpType, identifier));
        int count = Objects.requireNonNullElse(attemptCount, 1L).intValue();

        if (count == 1 || count >= MAX_ATTEMPTS) {
            redisTemplate.expire(buildAttemptKey(otpType, identifier), blockDuration);
        }
    }

    @Override
    public int getRequestCount(String otpType, String identifier) {
        String count = redisTemplate.opsForValue().get(buildRequestLimitKey(otpType, identifier));
        return count != null ? Integer.parseInt(count) : 0;
    }

    @Override
    public void incrementRequestCount(String otpType, String identifier, Duration requestLimitDuration) {
        Long requestCount = redisTemplate.opsForValue().increment(buildRequestLimitKey(otpType, identifier));
        int count = Objects.requireNonNullElse(requestCount, 1L).intValue();

        if (count == 1) {
            redisTemplate.expire(buildRequestLimitKey(otpType, identifier), requestLimitDuration);
        }
    }

    @Override
    public Duration getOtpExpiration(String otpType) {
        return switch (otpType) {
            case "password-reset", "password-change", "email-confirm" -> Duration.ofMinutes(5);
            default -> Duration.ofMinutes(10);
        };
    }

    private String buildOtpKey(String otpType, String identifier) {
        return OTP_PREFIX + otpType + ":" + identifier;
    }

    private String buildAttemptKey(String otpType, String identifier) {
        return ATTEMPT_PREFIX + otpType + ":" + identifier;
    }

    private String buildRequestLimitKey(String otpType, String identifier) {
        return REQUEST_LIMIT_PREFIX + otpType + ":" + identifier;
    }
}
