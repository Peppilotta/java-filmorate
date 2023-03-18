package ru.yandex.practicum.filmorate.sevice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
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

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        log.info("Create request for film {}", film);
        validateDateCreation(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Update request for film {}", film);
        validateDateCreation(film);
        return filmStorage.update(film);
    }

    public List<Film> getFilms() {
        log.info("GET request - all films");
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        log.info("GET request - film with id={}", id);
        return filmStorage.getFilm(id);
    }

    public Set<Genre> getGenres() {
        log.info("GET request - all genres");
        return filmStorage.getGenres();
    }

    public Genre getGenre(int id) {
        log.info("GET request - genre with id={}", id);
        return filmStorage.getGenre(id);
    }

    public Set<Mpa> getMpas() {
        log.info("GET request - all MPA");
        return filmStorage.getMpas();
    }

    public Mpa getMpa(int id) {
        log.info("GET request - MPA with id={}", id);
        return filmStorage.getMpa(id);
    }

    public void addLike(long filmId, long userId) {
        log.info("Add like for film id={} from user id={}", filmId, userId);
        userStorage.containsUser(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        log.info("Delete like for film id={} from user id={}", filmId, userId);
        userStorage.containsUser(userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getCountFavoriteFilms(int count) {
        log.info("GET request - popular films, highest {}", count);
        return filmStorage.getFavoriteFilms(count);
    }

    private void validateDateCreation(Film film) {
        if (film.getReleaseDate().isBefore(DATE_OF_FIRST_FILM)) {
            throw new ValidationExceptions("Bad date");
        }
    }
}