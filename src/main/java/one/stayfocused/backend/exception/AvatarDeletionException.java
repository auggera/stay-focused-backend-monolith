package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class AvatarDeletionException extends ApplicationException{

    public AvatarDeletionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
