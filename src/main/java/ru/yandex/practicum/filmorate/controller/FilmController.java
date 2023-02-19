package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private long meter = 1L;

    @PostMapping("/films")
    public Film create(@Valid @RequestBody final Film film) {
        validate(film);
        film.setId(meter);
        films.put(meter, film);
        meter++;
        log.info("POST request for film {}", film);
        return film;
    }

    @PutMapping("/films")
    public Film update(@Valid @RequestBody final Film film) {
        validate(film);
        long id = film.getId();
        log.info("PUT request for film {}", film);
        if (films.containsKey(id)) {
            films.put(id, film);
            return film;
        } else {
            throw new ValidationException("Wrong user id.");
        }
    }

    @GetMapping("/films")
    public List<Film> getAll() {
        log.info("GET request");
        return new ArrayList<>(films.values());
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse("28.12.1895",
                DateTimeFormatter.ofPattern("dd.MM.yyyy")))) {
            throw new ValidationExceptions("Bad date");
        }
    }
}