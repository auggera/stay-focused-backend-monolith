package one.stayfocused.backend.service;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.IncorrectCurrentPasswordException;
import one.stayfocused.backend.exception.TokenNotFoundException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class PasswordServiceImpl implements PasswordService {
    private static final String TOKEN_PREFIX = "token:";
    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";
    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request cannot be null";
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void verifyCurrentPassword(Long userId, PasswordVerificationRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user = getUserByIdInternal(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IncorrectCurrentPasswordException();
        }

        String tokenKey = tokenKeyBuilder(userId, OTP_TYPE_PASSWORD_CHANGE);
        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(tokenKey, token, TOKEN_EXPIRATION);
    }

    @Override
    public void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user =  getUserByIdInternal(userId);

        String token = retrieveToken(userId, OTP_TYPE_PASSWORD_CHANGE);

        if (token == null) {
            throw new TokenNotFoundException("Token not found or expired");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        deleteToken(userId, OTP_TYPE_PASSWORD_CHANGE);
    }

    @Override
    public void requestChangePasswordWithOtp(Long userId) {
        User user = getUserByIdInternal(userId);
        sendOtp(user.getEmail(), OTP_TYPE_PASSWORD_CHANGE);
    }

    @Override
    public void changePasswordWithOtp(Long userId, PasswordChangeWithOtpRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user = getUserByIdInternal(userId);

        otpService.validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), request.otpCode());
        updatePassword(user, request.newPassword());
    }

    @Override
    public void requestResetPasswordWithOtp(PasswordResetRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        getUserByEmailInternal(request.email());
        sendOtp(request.email(), OTP_TYPE_PASSWORD_RESET);
    }

    @Override
    public void resetPasswordWithOtp(PasswordResetWithOtpRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user = getUserByEmailInternal(request.email());

        otpService.validateOtp(OTP_TYPE_PASSWORD_RESET, request.email(), request.otpCode());
        updatePassword(user, request.newPassword());
    }

    private User getUserByIdInternal(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private User getUserByEmailInternal(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void sendOtp(String email, String otpType) {
        String otp = otpService.generateOtp(otpType, email);
        emailService.sendOtp(email, otp);
    }

    private String tokenKeyBuilder(Long userId, String otpType) {
        return TOKEN_PREFIX + otpType + ":" + userId;
    }

    private String retrieveToken(Long userId, String otpType) {
        return redisTemplate.opsForValue().get(tokenKeyBuilder(userId, otpType));
    }

    private void deleteToken(Long userId, String otpType) {
        redisTemplate.delete(tokenKeyBuilder(userId, otpType));
    }
}
