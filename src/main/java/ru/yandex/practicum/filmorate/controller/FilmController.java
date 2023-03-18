package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.sevice.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping("/films")
    public Film create(@RequestBody @Valid final Film film) {
        return filmService.create(film);
    }

    @PutMapping("/films")
    public Film update(@RequestBody @Valid final Film film) {
        return filmService.update(film);
    }

    @GetMapping("/films")
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/genres")
    public Set<Genre> getGenres() {
        return filmService.getGenres();
    }

    @GetMapping("/mpa")
    public Set<Mpa> getMpas() {
        return filmService.getMpas();
    }

    @GetMapping("/films/{id}")
    public Film getFilm(@PathVariable @Positive long id) {
        return filmService.getFilm(id);
    }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable @Positive int id) {
        return filmService.getGenre(id);
    }

    @GetMapping("/mpa/{id}")
    public Mpa getMpa(@PathVariable @Positive int id) {
        return filmService.getMpa(id);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") @PositiveOrZero int count) {
        return filmService.getCountFavoriteFilms(count);
    }
}