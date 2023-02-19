package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    static FilmController filmController = new FilmController();
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
    void validate_BadReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Too long description. Too long description. " +
                "Too long description. Too long description. Too long description.");
        film.setReleaseDate(LocalDate.parse("28.12.1885",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        Exception badName = assertThrows(ValidationExceptions.class,
                () -> filmController.validate(film));
        assertEquals("Bad date", badName.getMessage());
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