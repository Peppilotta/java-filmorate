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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.sevice.ValidationOfInputNumbers;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private final InMemoryFilmStorage filmStorage;
    private final ValidationOfInputNumbers validationOfInputNumbers;

    @Autowired
    public FilmController(InMemoryFilmStorage filmStorage, ValidationOfInputNumbers validationOfInputNumbers) {
        this.filmStorage = filmStorage;
        this.validationOfInputNumbers = validationOfInputNumbers;
    }

    @PostMapping
    public Film create(@Valid @RequestBody final Film film) {
        log.info("POST request for film {}", film);
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody final Film film) {
        log.info("PUT request for film {}", film);
        return filmStorage.update(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("GET request - all films");
        return filmStorage.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable String id) {
        log.info("GET request - films with id={}", id);
        validationOfInputNumbers.validateIdIsNumber(id, "Film");
        long filmId = Long.parseLong(id);
        validationOfInputNumbers.validatePositiveInputNumber(filmId, "Film");
        filmStorage.testIfExistFilmWithId(filmId);
        return filmStorage.getFilm(filmId);
    }
}