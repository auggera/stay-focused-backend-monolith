package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class TokenNotFoundException extends ApplicationException {
    public TokenNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
