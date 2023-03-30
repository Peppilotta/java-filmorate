package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmControllerTest {
    static FilmDbStorage filmStorage;
    private static Validator validator;

    @BeforeEach
    void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_StandardBehavior() {
        final Film film = Film.builder()
                .name("Belle Maman")
                .description("About combining an incompatible")
                .releaseDate(LocalDate.now())
                .duration(102L)
                .build();
        Set<ConstraintViolation<Film>> constraintViolations =
                validator.validate(film);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    void validate_EmptyName() {
        final Film film = Film.builder()
                .name("")
                .description("About combining an incompatible")
                .releaseDate(LocalDate.now())
                .duration(102L)
                .build();
        Set<ConstraintViolation<Film>> constraintViolations =
                validator.validate(film);
        assertEquals(1, constraintViolations.size());
        assertEquals("Name can't be empty", constraintViolations.iterator().next().
                getMessage());
    }

    @Test
    void validate_LongDescription() {
        final Film film = Film.builder()
                .name("Belle Maman")
                .description("Too long description. ".repeat(10))
                .releaseDate(LocalDate.now())
                .duration(102L)
                .build();
        Set<ConstraintViolation<Film>> constraintViolations =
                validator.validate(film);
        assertEquals(1, constraintViolations.size());
        assertEquals("Description size mast be between 1 and 200", constraintViolations.iterator().next().
                getMessage());
    }

    @Test
    void validate_EmptyDescription() {
        final Film film = Film.builder()
                .name("Belle Maman")
                .description("")
                .releaseDate(LocalDate.now())
                .duration(102L)
                .build();
        Set<ConstraintViolation<Film>> constraintViolations =
                validator.validate(film);
        assertEquals(1, constraintViolations.size());
        assertEquals("Description size mast be between 1 and 200", constraintViolations.iterator().next().
                getMessage());
    }

    @Test
    void validate_NegativeDuration() {
        final Film film = Film.builder()
                .name("Belle Maman")
                .description("About combining an incompatible")
                .releaseDate(LocalDate.now())
                .duration(-4L)
                .build();
        Set<ConstraintViolation<Film>> constraintViolations =
                validator.validate(film);
        assertEquals(1, constraintViolations.size());
        assertEquals("Bad duration", constraintViolations.iterator().next().
                getMessage());
    }
}