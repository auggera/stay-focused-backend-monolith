package one.stayfocused.backend.dto;


import jakarta.validation.*;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OAuthRegisterRequestDtoTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    public static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void shouldPassValidation_whenFieldsAreValid() {
        OAuthRegisterRequestDto request = new OAuthRegisterRequestDto();
        request.setEmail("valid@email.com");
        request.setName("Valid Name");
        request.setProvider("Valid Provider");
        request.setProviderId("Valid Provider ID");

        Set<ConstraintViolation<OAuthRegisterRequestDto>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void shouldFailValidation_whenFieldsAreEmpty() {
        OAuthRegisterRequestDto request = new OAuthRegisterRequestDto();
        request.setEmail("");
        request.setName("");
        request.setProvider("");
        request.setProviderId("");

        Set<ConstraintViolation<OAuthRegisterRequestDto>> violations = validator.validate(request);
        assertEquals(4, violations.size());
    }

    @Test
    void shouldFailValidation_whenFieldsAreNull() {
        OAuthRegisterRequestDto request = new OAuthRegisterRequestDto();
        request.setEmail(null);
        request.setName(null);
        request.setProvider(null);
        request.setProviderId(null);

        Set<ConstraintViolation<OAuthRegisterRequestDto>> violations = validator.validate(request);
        assertEquals(4, violations.size());
    }
}
