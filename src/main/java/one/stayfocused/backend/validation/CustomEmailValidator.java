package one.stayfocused.backend.validation;

import org.apache.commons.validator.routines.EmailValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomEmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (email == null || email.isBlank()) {
            return true; // Let @NotBlank handle this case
        }
        return EmailValidator.getInstance().isValid(email);
    }
}
