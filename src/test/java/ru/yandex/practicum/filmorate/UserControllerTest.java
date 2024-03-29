package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserControllerTest {
    private static Validator validator;

    @BeforeEach
    void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_StandardBehavior() {
        final User user = User.builder()
                .email("fet@mail.ru")
                .login("fet")
                .birthday(LocalDate.now().minusYears(35))
                .name("Theodor")
                .build();
        Set<ConstraintViolation<User>> constraintViolations =
                validator.validate(user);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    void validate_EmptyEmail() {
        final User user = User.builder()
                .email("")
                .login("fet")
                .birthday(LocalDate.now().minusYears(35))
                .name("Theodor")
                .build();
        Set<ConstraintViolation<User>> constraintViolations =
                validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Email absent", constraintViolations.iterator().next().
                getMessage());
    }

    @Test
    void validate_WrongEmail() {
        final User user = User.builder()
                .email("mail.ru")
                .login("fet")
                .birthday(LocalDate.now().minusYears(35))
                .name("Theodor")
                .build();
        Set<ConstraintViolation<User>> constraintViolations =
                validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Wrong email", constraintViolations.iterator().next().
                getMessage());
    }

    @Test
    void validate_LongDescription() {
        final User user = User.builder()
                .email("fet@mail.ru")
                .login("")
                .birthday(LocalDate.now().minusYears(35))
                .name("Theodor")
                .build();
        Set<ConstraintViolation<User>> constraintViolations =
                validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Wrong login", constraintViolations.iterator().next().
                getMessage());
    }
}