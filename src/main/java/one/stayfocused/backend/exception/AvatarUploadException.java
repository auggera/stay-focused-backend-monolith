package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class AvatarUploadException extends ApplicationException {
    public AvatarUploadException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
