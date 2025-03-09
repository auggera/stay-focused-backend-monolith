package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends ApplicationException {

    public InvalidOtpException() {
        super("OTP code is incorrect", HttpStatus.UNAUTHORIZED);
    }
}
