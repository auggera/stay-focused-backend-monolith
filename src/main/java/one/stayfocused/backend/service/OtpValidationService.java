package one.stayfocused.backend.service;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.exception.InvalidOtpException;
import one.stayfocused.backend.exception.OtpBlockedException;
import one.stayfocused.backend.exception.OtpNotFoundException;
import one.stayfocused.backend.repository.OtpRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OtpValidationService {

    private final OtpRepository otpRepository;
    private final OtpRateLimiter otpRateLimiterService;

    public void validateOtp(String otpType, String identifier, String otp) {
        if (otpRateLimiterService.isBlocked(otpType, identifier)) {
            throw new OtpBlockedException();
        }

        String storedOtp = otpRepository.getOtp(otpType, identifier);

        if (storedOtp == null) {
            throw new OtpNotFoundException();
        }

        if (!storedOtp.equals(otp)) {
            throw new InvalidOtpException();
        }

        otpRepository.deleteOtp(otpType, identifier);
    }
}
