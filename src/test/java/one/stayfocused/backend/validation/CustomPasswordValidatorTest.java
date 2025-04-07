package one.stayfocused.backend.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CustomPasswordValidatorTest {

    private final CustomPasswordValidator validator = new CustomPasswordValidator();
    private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

    @ParameterizedTest
    @MethodSource("validPasswordProvider")
    void validPasswordShouldReturnTrue(String password) {
        assertTrue(validator.isValid(password, context), "Password '" + password + "' should be valid");
    }

    static Stream<Arguments> validPasswordProvider() {
        return Stream.of(
            Arguments.of("ValidPassword#1"),
            Arguments.of(""), // handles @NotBlank
            Arguments.of((Object) null) // handles @NotBlank
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void invalidPasswordShouldReturnFalse(String password) {
        assertFalse(validator.isValid(password, context), "Password '" + password + "' should be invalid");
    }

    static Stream<Arguments> invalidPasswordProvider() {
        return Stream.of(
            Arguments.of("invalid"),
            Arguments.of("invalid123"),
            Arguments.of("invalid#"),
            Arguments.of("invalid#123"),
            Arguments.of("INVALID#123"),
            Arguments.of("123124342###"),
            Arguments.of("Short#1")
        );
    }
}
