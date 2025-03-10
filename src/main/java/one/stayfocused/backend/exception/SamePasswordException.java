package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class SamePasswordException extends ApplicationException {
    public SamePasswordException() {
        super("New password cannot be the same as the current password.", HttpStatus.CONFLICT);
    }
}
