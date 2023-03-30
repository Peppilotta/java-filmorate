package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ItemDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate DATE_OF_FIRST_FILM = LocalDate.parse("28.12.1895",
            DateTimeFormatter.ofPattern("dd.MM.yyyy"));

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film create(Film film) {
        log.info("Create request for film {}", film);
        containsMpa(film.getMpa().getId());
        Set<Genre> genres = film.getGenres();
        if (!genres.isEmpty()) {
            genres.forEach(genre -> containsGenre(genre.getId()));
        }
        validateDateCreation(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Update request for film {}", film);
        containsFilm(film.getId());
        containsMpa(film.getMpa().getId());
        Set<Genre> genres = film.getGenres();
        if (!genres.isEmpty()) {
            genres.forEach(genre -> containsGenre(genre.getId()));
        }
        validateDateCreation(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        log.info("GET request - all films");
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        log.info("GET request - film with id={}", id);
        containsFilm(id);
        return filmStorage.getFilm(id);
    }

    public void addLike(long filmId, long userId) {
        log.info("Add like for film id={} from user id={}", filmId, userId);
        containsUser(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        log.info("Delete like for film id={} from user id={}", filmId, userId);
        containsFilm(filmId);
        containsUser(userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getTheMostPopularFilms(int count) {
        log.info("GET request - popular films, highest {}", count);
        return filmStorage.getTheMostPopularFilms(count);
    }

    private void validateDateCreation(Film film) {
        if (film.getReleaseDate().isBefore(DATE_OF_FIRST_FILM)) {
            throw new ValidationExceptions("Bad date");
        }
    }

    private void containsUser(long id) {
        if (!userStorage.containsUser(id)) {
            throw new ItemDoesNotExistException("User with id=" + id + " not exist. ");
        }
    }

    private void containsFilm(long id) {
        if (!filmStorage.containsFilm(id)) {
            throw new ItemDoesNotExistException("Film with id=" + id + " not exist. ");
        }
    }

    private void containsMpa(long id) {
        if (!mpaStorage.containsMpa(id)) {
            throw new ItemDoesNotExistException("MPA with id=" + id + " not exist. ");
        }
    }

    private void containsGenre(long id) {
        if (!genreStorage.containsGenre(id)) {
            throw new ItemDoesNotExistException("Genre with id=" + id + " not exist. ");
        }
    }
}