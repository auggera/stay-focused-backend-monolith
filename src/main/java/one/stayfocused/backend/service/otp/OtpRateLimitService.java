package one.stayfocused.backend.service.otp;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class OtpRateLimitService implements OtpRateLimiter {

    private final OtpRepository otpRepository;

    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_REQUESTS_PER_DAY = 10;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(10);
    private static final Duration REQUEST_LIMIT_DURATION = Duration.ofDays(1);

    @Override
    public boolean isBlocked(String otpType, String identifier) {
        return otpRepository.getFailedAttempts(otpType, identifier) >= MAX_ATTEMPTS;
    }

    @Override
    public boolean isRequestLimitExceeded(String otpType, String identifier) {
        return otpRepository.getRequestCount(otpType, identifier) >= MAX_REQUESTS_PER_DAY;
    }

    @Override
    public void incrementFailedAttempts(String otpType, String identifier) {
        otpRepository.incrementFailedAttempts(otpType, identifier, BLOCK_DURATION);
    }

    @Override
    public void incrementOtpRequestCount(String otpType, String identifier) {
        otpRepository.incrementRequestCount(otpType, identifier,  REQUEST_LIMIT_DURATION);
    }
}
