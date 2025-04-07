package one.stayfocused.backend.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CustomNameValidatorTest {

    private final CustomNameValidator validator = new CustomNameValidator();
    private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

    @ParameterizedTest
    @MethodSource(value = "validNameProvider")
    void validNameShouldReturnTrue(String validName) {
        assertTrue(validator.isValid(validName, context));
    }

    static Stream<Arguments> validNameProvider() {
        return Stream.of(
            Arguments.of("John Doe"),
            Arguments.of("O'Connor"),
            Arguments.of("Anne-Marie"),
            Arguments.of(""), // handles @NotBlank
            Arguments.of( (Object) null) // handles @NotBlank
        );
    }

    @ParameterizedTest
    @MethodSource(value = "invalidNameProvider")
    void invalidNameShouldReturnFalse(String invalidName) {
        assertFalse(validator.isValid(invalidName, context));
    }

    static Stream<Arguments> invalidNameProvider() {
        return Stream.of(
            Arguments.of("John123"),
            Arguments.of("Doe@"),
            Arguments.of(".")
        );
    }
}
