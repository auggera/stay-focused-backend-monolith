package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class AvatarDeletionNotAllowedException extends ApplicationException {
    public AvatarDeletionNotAllowedException() {
        super("Cannot delete default avatar", HttpStatus.BAD_REQUEST);
    }
}
