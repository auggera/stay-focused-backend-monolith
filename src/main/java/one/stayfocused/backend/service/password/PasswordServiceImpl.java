package one.stayfocused.backend.service.password;

import lombok.RequiredArgsConstructor;
import one.stayfocused.backend.dto.*;
import one.stayfocused.backend.exception.IncorrectCurrentPasswordException;
import one.stayfocused.backend.exception.SamePasswordException;
import one.stayfocused.backend.exception.TokenNotFoundException;
import one.stayfocused.backend.exception.UserNotFoundException;
import one.stayfocused.backend.model.User;
import one.stayfocused.backend.repository.EphemeralTokenRepository;
import one.stayfocused.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Service
public class PasswordServiceImpl implements PasswordService {

    private static final String REQUEST_NULL_ERROR_MESSAGE = "Request must not be null";
    private static final String TOKEN_TYPE_PASSWORD_CHANGE = "password-change";
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final EphemeralTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void verifyCurrentPassword(Long userId, PasswordVerificationRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user = getUserByIdInternal(userId);

        if (!isSamePasswordAsCurrent(request.currentPassword(), user.getPassword())) {
            throw new IncorrectCurrentPasswordException();
        }

        tokenRepository.deleteToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());

        String token = UUID.randomUUID().toString();
        tokenRepository.saveToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail(), token, TOKEN_EXPIRATION);
    }

    @Override
    @Transactional
    public void changePasswordAfterVerification(Long userId, PasswordUpdateRequestDto request) {
        requireNonNull(request, REQUEST_NULL_ERROR_MESSAGE);
        User user =  getUserByIdInternal(userId);

        String token = tokenRepository.getToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());

        if (token == null) {
            throw new TokenNotFoundException("Token not found or expired");
        }

        if (isSamePasswordAsCurrent(request.newPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        updatePassword(user, request.newPassword());
        userRepository.save(user);

        tokenRepository.deleteToken(TOKEN_TYPE_PASSWORD_CHANGE, user.getEmail());
    }

    private User getUserByIdInternal(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    private boolean isSamePasswordAsCurrent(String newPassword, String currentPassword) {
        return passwordEncoder.matches(newPassword, currentPassword);
    }
}
