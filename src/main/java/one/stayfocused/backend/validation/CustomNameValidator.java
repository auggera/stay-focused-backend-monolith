package one.stayfocused.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomNameValidator implements ConstraintValidator<ValidName, String> {

    private static final String NAME_PATTERN = "^[A-Za-z\\s'-]+$";

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if (name == null || name.isBlank()) {
            return true; // Let @NotBlanc handle this case
        }
        return name.matches(NAME_PATTERN);
    }
}
