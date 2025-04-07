package one.stayfocused.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class CustomPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-\\[\\]{};':\"\\\\|,.<>/?].*");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null || password.trim().isEmpty()) {
            return true; // Let @NotBlank handle the case
        }

        return password.length() >= 8
                && UPPERCASE.matcher(password).matches()
                && LOWERCASE.matcher(password).matches()
                && DIGIT.matcher(password).matches()
                && SPECIAL_CHAR.matcher(password).matches();
    }
}
