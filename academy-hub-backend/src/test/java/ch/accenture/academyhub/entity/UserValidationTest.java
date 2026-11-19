package ch.accenture.academyhub.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void acceptsValidLearnerProfile() {
        User user = new User("Test", "Learner", Role.LEARNER);
        user.updateProfile(
                "learner@example.com",
                "Zurich",
                "Application Development",
                3
        );

        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void acceptsMissingOptionalFieldsForBothRoles() {
        for (Role role : Role.values()) {
            User user = new User("Test", "User", role);

            assertTrue(
                    validator.validate(user).isEmpty(),
                    "Optional fields should be allowed for " + role
            );
        }
    }

    @Test
    void acceptsLocationAndProfessionAtMaximumLength() {
        User user = new User("Test", "Learner", Role.LEARNER);
        user.updateProfile(
                "learner@example.com",
                "L".repeat(100),
                "P".repeat(150),
                1
        );

        assertTrue(validator.validate(user).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidProfiles")
    void rejectsInvalidProfile(
            String email,
            String location,
            String profession,
            Integer year,
            String expectedField
    ) {
        User user = new User("Test", "Learner", Role.LEARNER);
        user.updateProfile(email, location, profession, year);

        assertTrue(
                validator.validate(user).stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals(expectedField)
                        ),
                "Expected a validation error for " + expectedField
        );
    }

    static Stream<Arguments> invalidProfiles() {
        return Stream.of(
                Arguments.of("invalid-email", null, null, null, "email"),
                Arguments.of("", null, null, null, "email"),
                Arguments.of("   ", null, null, null, "email"),
                Arguments.of("a".repeat(255), null, null, null, "email"),
                Arguments.of(null, "", null, null, "location"),
                Arguments.of(null, " \t ", null, null, "location"),
                Arguments.of(null, "L".repeat(101), null, null, "location"),
                Arguments.of(null, null, "", null, "profession"),
                Arguments.of(null, null, "   ", null, "profession"),
                Arguments.of(null, null, "P".repeat(151), null, "profession"),
                Arguments.of(null, null, null, 0, "apprenticeshipYear"),
                Arguments.of(null, null, null, -1, "apprenticeshipYear")
        );
    }
}