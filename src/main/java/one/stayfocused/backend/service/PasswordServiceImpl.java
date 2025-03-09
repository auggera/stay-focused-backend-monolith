package one.stayfocused.backend.service;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.dto.PasswordChangeWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordResetWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordUpdateRequestDto;
import one.stayfocused.backend.dto.PasswordVerificationRequestDto;
import one.stayfocused.backend.exception.IncorrectCurrentPasswordException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class PasswordServiceImpl implements PasswordService {
    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";
    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request cannot be null";

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void verifyCurrentPassword(Long userId, PasswordVerificationRequestDto request) {
        User user = getUserByIdInternal(userId);
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IncorrectCurrentPasswordException();
        }
    }

    @Override
    public void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request) {
        User user =  getUserByIdInternal(userId);
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void requestChangePasswordWithOtp(Long userId) {
        User user = getUserByIdInternal(userId);
        sendOtp(user.getEmail(), OTP_TYPE_PASSWORD_CHANGE);
    }

    @Override
    public void changePasswordWithOtp(Long userId, PasswordChangeWithOtpRequestDto request) {
        User user = getUserByIdInternal(userId);
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

        otpService.validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), request.otpCode());
        updatePassword(user, request.newPassword());
    }

    @Override
    public void requestResetPasswordWithOtp(String email) {
        requireNonNull(email, REQUEST_NULL_ERROR_MESSAGE);

        getUserByEmailInternal(email);
        sendOtp(email, OTP_TYPE_PASSWORD_RESET);
    }

    @Override
    public void resetPasswordWithOtp(PasswordResetWithOtpRequestDto request) {
        User user = getUserByEmailInternal(request.email());
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);

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
}
