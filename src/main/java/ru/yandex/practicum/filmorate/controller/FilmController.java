package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class FilmController extends ProgenitorController<Film> {

    @PostMapping("/films")
    @Override
    public Film create(@RequestBody @Valid final Film film) {
        log.info("POST request for film {}", film);
        return super.create(film);
    }

    @PutMapping("/films")
    @Override
    public Film update(@RequestBody @Valid final Film film) {
        log.info("PUT request for film {}", film);
        Film real = super.update(film);
        if (Objects.isNull(real)) {
            throw new ValidationExceptions("Wrong film id.");
        }
        return real;
    }

    @GetMapping("/films")
    @Override
    public List<Film> getAll() {
        log.info("GET request");
        return new ArrayList<>(super.getAll());
    }

    @Override
    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse("28.12.1895",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")))) {
            throw new ValidationExceptions("Bad date");
        }
    }
}