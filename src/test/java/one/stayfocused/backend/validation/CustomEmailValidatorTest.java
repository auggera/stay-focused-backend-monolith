package one.stayfocused.backend.validation;


import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CustomEmailValidatorTest {

    private final CustomEmailValidator validator = new CustomEmailValidator();
    private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

    @ParameterizedTest
    @MethodSource(value = "validEmailProvider")
    void validEmailShouldReturnTrue(String email) {
        assertTrue(validator.isValid(email, context));
    }

    static Stream<Arguments> validEmailProvider() {
        return Stream.of(
                Arguments.of("valid@email.com"),
                Arguments.of(""), // handles @NotBlank
                Arguments.of( (Object) null) // handles @NotBlank
        );
    }

    @ParameterizedTest
    @MethodSource(value = "invalidEmailProvider")
    void invalidEmailShouldReturnFalse(String email) {
        assertFalse(validator.isValid(email, context), "Email '" + email + "' should be invalid");
    }

    static Stream<Arguments> invalidEmailProvider() {
        return Stream.of(
                Arguments.of("invalid.com"),
                Arguments.of("invalid@.com"),
                Arguments.of("invalid@com"),
                Arguments.of("invalid@email@example.com"),
                Arguments.of("invalid@example,com"),
                Arguments.of("invalid@example.c"),
                Arguments.of("invalid email@example.com"),
                Arguments.of("invalid@example.com."),
                Arguments.of("invalid@example..com")
        );
    }
}
