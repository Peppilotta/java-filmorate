package ru.yandex.practicum.filmorate.sevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FilmService {
    private final LocalDate filmsStart = LocalDate.parse("28.12.1895",
            DateTimeFormatter.ofPattern("dd.MM.yyyy"));

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        validateDateCreation(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateDateCreation(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id);
    }

    private void validateDateCreation(Film film) {
        if (film.getReleaseDate().isBefore(filmsStart)) {
            throw new ValidationExceptions("Bad date");
        }
    }
}