package one.stayfocused.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserNotFoundException extends ApplicationException {
    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        this.userId = userId;
    }
}
