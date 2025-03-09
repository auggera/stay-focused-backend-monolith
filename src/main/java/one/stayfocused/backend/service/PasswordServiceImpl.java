package one.stayfocused.backend.service;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.exception.IncorrectCurrentPasswordException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PasswordServiceImpl implements PasswordService {
    private static final String OTP_TYPE_PASSWORD_RESET = "password-reset";
    private static final String OTP_TYPE_PASSWORD_CHANGE = "password-change";

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void verifyCurrentPassword(Long userId, String currentPassword) {
        User user = getUserByIdInternal(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IncorrectCurrentPasswordException();
        }
    }

    @Override
    public void changePasswordAfterVerification(Long userId, String newPassword) {
        User user =  getUserByIdInternal(userId);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void requestChangePasswordWithOtp(Long userId) {
        User user = getUserByIdInternal(userId);
        sendOtp(user.getEmail(), OTP_TYPE_PASSWORD_CHANGE);
    }

    @Override
    public void changePasswordWithOtp(Long userId, String otpCode, String newPassword) {
        User user = getUserByIdInternal(userId);
        otpService.validateOtp(OTP_TYPE_PASSWORD_RESET, user.getEmail(), otpCode);
        updatePassword(user, newPassword);
    }

    @Override
    public void requestResetPasswordWithOtp(String email) {
        getUserByEmailInternal(email);
        sendOtp(email, OTP_TYPE_PASSWORD_RESET);
    }

    @Override
    public void resetPasswordWithOtp(String email, String otpCode, String newPassword) {
        User user = getUserByEmailInternal(email);
        otpService.validateOtp(OTP_TYPE_PASSWORD_RESET, email, otpCode);
        updatePassword(user, newPassword);
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
