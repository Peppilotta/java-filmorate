package ru.yandex.practicum.filmorate.sevice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final LocalDate filmsStart = LocalDate.parse("28.12.1895",
            DateTimeFormatter.ofPattern("dd.MM.yyyy"));

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
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

    public void addLike(long filmId, long userId) {
        log.info("Add like for film id={} from user id={}", filmId, userId);
        filmStorage.containsFilm(filmId);
        userStorage.containsUser(userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLikeIds();
        likes.add(userId);
        film.setLikeIds(likes);
    }

    public void deleteLike(long filmId, long userId) {
        log.info("Delete like for film id={} from user id={}", filmId, userId);
        filmStorage.containsFilm(filmId);
        userStorage.containsUser(userId);
        Set<Long> likes = filmStorage.getFilm(filmId).getLikeIds();
        likes.remove(userId);
    }

    public List<Film> getCountFavoriteFilms(int count) {
        log.info("GET request - popular films, highest {}", count);
        List<Film> allFilms = filmStorage.getFilms();
        return allFilms
                .stream()
                .sorted(Comparator.comparing(f -> f.getLikeIds().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateDateCreation(Film film) {
        if (film.getReleaseDate().isBefore(filmsStart)) {
            throw new ValidationExceptions("Bad date");
        }
    }
}