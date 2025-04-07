package one.stayfocused.backend.service.otp;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.exception.OtpRequestLimitExceededException;
import one.stayfocused.backend.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@RequiredArgsConstructor
@Service
public class OtpGeneratorService implements OtpGenerator {

    private final OtpRepository otpRepository;
    private final OtpRateLimiter otpRateLimitService;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateOtp(String otpType, String identifier) {
        if (otpRateLimitService.isRequestLimitExceeded(otpType, identifier)) {
            throw new OtpRequestLimitExceededException();
        }

        otpRepository.deleteOtp(otpType, identifier);

        String otp = generate();
        Duration expiration = otpRepository.getOtpExpiration(otpType);

        otpRepository.saveOtp(otpType, identifier, otp, expiration);
        otpRateLimitService.incrementOtpRequestCount(otpType, identifier);

        return otp;
    }

    private String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
