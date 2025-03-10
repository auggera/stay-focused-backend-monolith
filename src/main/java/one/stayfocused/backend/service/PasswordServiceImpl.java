package one.stayfocused.backend.service;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.IncorrectCurrentPasswordException;
import one.stayfocused.backend.exception.SamePasswordException;
import one.stayfocused.backend.exception.TokenNotFoundException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class PasswordServiceImpl implements PasswordService {
    private static final String TOKEN_PREFIX = "token:";
    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";
    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request must not be null";
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
        deleteToken(tokenKey);

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(tokenKey, token, TOKEN_EXPIRATION);
    }

    @Override
    @Transactional
    public void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user =  getUserByIdInternal(userId);

        String tokenKey = tokenKeyBuilder(userId, OTP_TYPE_PASSWORD_CHANGE);
        String token = retrieveToken(tokenKey);

        if (token == null) {
            throw new TokenNotFoundException("Token not found or expired");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        deleteToken(tokenKey);
    }

    @Override
    public void requestChangePasswordWithOtp(Long userId) {
        User user = getUserByIdInternal(userId);
        sendOtp(user.getEmail(), OTP_TYPE_PASSWORD_CHANGE);
    }

    @Override
    @Transactional
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
    @Transactional
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
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException();
        }

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

    private String retrieveToken(String tokenKey) {
        return redisTemplate.opsForValue().get(tokenKey);
    }

    private void deleteToken(String tokenKey) {
        redisTemplate.delete(tokenKey);
    }
}
