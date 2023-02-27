package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.IdIsNotNumber;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.sevice.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody final Film film) {
        log.info("POST request for film {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody final Film film) {
        log.info("PUT request for film {}", film);
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("GET request - all films");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable String id) {
        log.info("GET request - films with id={}", id);
        long filmId = validateIdIsNumber(id);
        validatePositive(filmId);
        return filmService.getFilm(filmId);
    }

    private void validatePositive(long id) {
        if (id < 0) {
            throw new IllegalInputId("Film id is not positive");
        }
    }

    private long validateIdIsNumber(String id) throws IdIsNotNumber {
        try {
            return Long.parseLong(id);
        } catch (IdIsNotNumber e) {
            throw new IdIsNotNumber("Film id must be a number");
        }
    }
}