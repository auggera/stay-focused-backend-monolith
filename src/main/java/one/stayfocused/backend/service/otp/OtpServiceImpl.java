package one.stayfocused.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.stayfocused.backend.exception.InvalidOtpException;
import one.stayfocused.backend.exception.OtpBlockedException;
import one.stayfocused.backend.exception.OtpNotFoundException;
import one.stayfocused.backend.exception.OtpRequestLimitExceededException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class OtpServiceImpl implements OtpService {
    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPT_PREFIX = "attempts:";
    private static final String REQUEST_LIMIT_PREFIX = "otp-request:";

    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_REQUESTS_PER_DAY = 10;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(10);
    private static final Duration REQUEST_LIMIT_DURATION = Duration.ofDays(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateOtp(String otpType, String identifier) {
        String requestLimitKey = buildRequestLimitKey(otpType, identifier);

        if (isRequestLimitExceeded(requestLimitKey)) {
            throw new OtpRequestLimitExceededException();
        }

        deleteOtp(otpType, identifier);

        String otp = String.format("%06d", random.nextInt(1_000_000));
        String key = buildOtpKey(otpType, identifier);
        Duration expiration = getOtpExpiration(otpType);

        redisTemplate.opsForValue().set(key, otp, expiration);
        incrementOtpRequestCount(requestLimitKey);

        return otp;
    }

    @Override
    public void validateOtp(String otpType, String identifier, String otp) {
        String otpKey = buildOtpKey(otpType, identifier);
        String attemptKey = buildAttemptKey(otpType, identifier);

        if (isBlocked(attemptKey)) {
            throw new OtpBlockedException();
        }

        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new OtpNotFoundException();
        }

        if (!storedOtp.equals(otp)) {
            incrementFailedAttempts(attemptKey);
            throw new InvalidOtpException();
        }

        redisTemplate.delete(List.of(otpKey, attemptKey));
    }

    @Override
    public void deleteOtp(String otpType, String identifier) {
        String key = buildOtpKey(otpType, identifier);
        redisTemplate.delete(key);
    }

    private void incrementFailedAttempts(String attemptKey) {
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        int attemptCount = Objects.requireNonNullElse(attempts, 1).intValue();

        if (attemptCount == 1 || attemptCount >= MAX_ATTEMPTS) {
            redisTemplate.expire(attemptKey, BLOCK_DURATION);
        }
    }

    private boolean isBlocked(String attemptKey) {
        String attempts = redisTemplate.opsForValue().get(attemptKey);
        try {
            return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            log.warn("...");
            return false;
        }
    }

    private void incrementOtpRequestCount(String requestLimitKey) {
       Long  requests  = redisTemplate.opsForValue().increment(requestLimitKey);
       int requestCount = Objects.requireNonNullElse(requests, 1).intValue();

        if (requestCount == 1) {
            redisTemplate.expire(requestLimitKey, REQUEST_LIMIT_DURATION);
        }
    }

    private boolean isRequestLimitExceeded(String requestLimitKey) {
        String count = redisTemplate.opsForValue().get(requestLimitKey);
        return count != null && Integer.parseInt(count) >= MAX_REQUESTS_PER_DAY;
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

    private Duration getOtpExpiration(String otpType) {
        return switch (otpType) {
            case "password-reset", "password-change", "email-confirm" -> Duration.ofMinutes(5);
            default -> Duration.ofMinutes(10);
        };
    }
}
