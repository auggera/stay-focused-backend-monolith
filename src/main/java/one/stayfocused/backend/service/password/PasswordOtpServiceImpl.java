package one.stayfocused.backend.service.password;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.dto.PasswordChangeWithOtpRequestDto;
import one.stayfocused.backend.dto.PasswordResetRequestDto;
import one.stayfocused.backend.dto.PasswordResetWithOtpRequestDto;
import one.stayfocused.backend.exception.SamePasswordException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import one.stayfocused.backend.service.EmailService;
import one.stayfocused.backend.service.otp.OtpGenerator;
import one.stayfocused.backend.service.otp.OtpValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class PasswordOtpServiceImpl implements PasswordOtpService {

    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";
    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request must not be null";

    private final UserRepository userRepository;
    private final OtpGenerator otpGenerator;
    private final OtpValidator otpValidator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


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

        otpValidator.validateOtp(OTP_TYPE_PASSWORD_CHANGE, user.getEmail(), request.otpCode());

        if (isSamePasswordAsCurrent(request.newPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        updatePassword(user, request.newPassword());
        userRepository.save(user);
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

        otpValidator.validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), request.otpCode());

        if (isSamePasswordAsCurrent(request.newPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        updatePassword(user, request.newPassword());
        userRepository.save(user);
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
    }

    private void sendOtp(String email, String otpType) {
        String otp = otpGenerator.generateOtp(otpType, email);
        emailService.sendOtp(email, otp);
    }

    private boolean isSamePasswordAsCurrent(String newPassword, String currentPassword) {
        return passwordEncoder.matches(newPassword, currentPassword);
    }
}
