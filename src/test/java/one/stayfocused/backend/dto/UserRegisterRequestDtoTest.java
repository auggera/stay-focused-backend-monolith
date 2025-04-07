package one.stayfocused.backend.dto;

import jakarta.validation.*;
import org.junit.jupiter.api.*;

import java.util.Set;

class UserRegisterRequestDtoTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void shouldPassValidation_whenFieldsAreValid() {
        UserRegisterRequestDto request = new UserRegisterRequestDto();
        request.setEmail("valid@email.com");
        request.setPassword("ValidPassword#1");
        request.setName("Valid Name");

        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validator.validate(request);
        Assertions.assertEquals(0, violations.size());
    }

    @Test
    void shouldFailValidation_whenFieldsAreEmpty() {
        UserRegisterRequestDto request = new UserRegisterRequestDto();
        request.setEmail("");
        request.setPassword("");
        request.setName("");

        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validator.validate(request);
        Assertions.assertEquals(3, violations.size());
    }

    @Test
    void shouldFailValidation_whenFieldsAreNull() {
        UserRegisterRequestDto request = new UserRegisterRequestDto();
        request.setEmail(null);
        request.setPassword(null);
        request.setName(null);

        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validator.validate(request);
        Assertions.assertEquals(3, violations.size());
    }
}
