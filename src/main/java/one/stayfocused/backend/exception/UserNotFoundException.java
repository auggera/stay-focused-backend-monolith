package one.stayfocused.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserNotFoundException extends ApplicationException {
    private final String identifier;

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId, HttpStatus.NOT_FOUND);
        this.identifier = "ID: " + userId;
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email, HttpStatus.NOT_FOUND);
        this.identifier = "Email: " + email;
    }
}
