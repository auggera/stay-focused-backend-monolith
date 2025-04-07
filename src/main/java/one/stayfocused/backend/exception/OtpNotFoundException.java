package one.stayfocused.backend.exception;

import org.springframework.http.HttpStatus;

public class OtpNotFoundException extends ApplicationException {

    public OtpNotFoundException() {
        super("OTP code not found or expired", HttpStatus.NOT_FOUND);
    }
}
