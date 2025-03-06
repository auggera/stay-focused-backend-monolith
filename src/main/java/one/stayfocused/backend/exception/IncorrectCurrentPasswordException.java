package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class IncorrectCurrentPasswordException extends ApplicationException {

    public IncorrectCurrentPasswordException() {
        super("Incorrect current password", HttpStatus.BAD_REQUEST);
    }
}
