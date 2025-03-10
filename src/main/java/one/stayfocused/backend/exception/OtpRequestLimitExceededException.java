package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class OtpRequestLimitExceededException extends ApplicationException {
    public OtpRequestLimitExceededException() {
        super("Too many OTP requests. Try again tomorrow.", HttpStatus.TOO_MANY_REQUESTS);
    }
}
