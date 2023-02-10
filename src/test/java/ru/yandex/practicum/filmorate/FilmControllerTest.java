package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmControllerTest {
    static FilmController filmController = new FilmController();

    @Test
    void validate_StandardBehavior() {
        final Film film = Film.builder()
                .name("Belle Maman")
                .description("About combining an incompatible")
                .releaseDate(LocalDate.now())
                .duration(102L)
                .build();
        filmController.validate(film);
    }

    @Test
    void validate_EmptyName() {
        Film film = new Film();
        Exception badName = assertThrows(ValidationException.class,
                () -> filmController.validate(film));
        assertEquals("Name can't be empty",badName.getMessage());
    }

    @Test
    void validate_LongDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Too long description. Too long description. " +
                "Too long description. Too long description. Too long description. " +
                "Too long description. Too long description. Too long description. " +
                "Too long description. Too long description. Too long description. " +
                "Too long description. Too long description. Too long description.");
        Exception badName = assertThrows(ValidationException.class,
                () -> filmController.validate(film));
        assertEquals("Description size mast be between 1 and 200",badName.getMessage());
    }

    @Test
    void validate_BadReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Too long description. Too long description. " +
                "Too long description. Too long description. Too long description.");
        film.setReleaseDate(LocalDate.parse("28.12.1885",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        Exception badName = assertThrows(ValidationException.class,
                () -> filmController.validate(film));
        assertEquals("Bad date",badName.getMessage());
    }

    @Test
    void validate_NegativeDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Too long description. Too long description. " +
                "Too long description. Too long description. Too long description.");
        film.setReleaseDate(LocalDate.parse("28.12.1985",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        film.setDuration(-4);
        Exception badName = assertThrows(ValidationException.class,
                () -> filmController.validate(film));
        assertEquals("Bad duration",badName.getMessage());
    }
}