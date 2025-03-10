package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class OtpBlockedException extends ApplicationException {
    public OtpBlockedException() {
        super("Too many failed attempts. Try again later.", HttpStatus.FORBIDDEN);
    }
}
