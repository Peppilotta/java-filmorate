package ru.yandex.practicum.filmorate.sevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(long filmId, long userId) {
        filmStorage.testIfExistFilmWithId(filmId);
        userStorage.testIfExistUserWithId(userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLikes();
        if (Objects.isNull(likes)) {
            likes = new HashSet<>();
        }
        likes.add(userId);
        film.setLikes(likes);
    }

    public void deleteLike(long filmId, long userId) {
        filmStorage.testIfExistFilmWithId(filmId);
        userStorage.testIfExistUserWithId(userId);
        Set<Long> likes = filmStorage.getFilm(filmId).getLikes();
        likes.remove(userId);
    }

    public List<Film> getCountFavoriteFilms(int count) {
        if (count < 0) {
            throw new IllegalInputId("Number of lines must be positive. ");
        }
        List<Film> allFilms = filmStorage.getFilms();
        int checkedCount = allFilms.size();
        if (checkedCount < count) {
            count = checkedCount;
        }

        allFilms.sort((o1, o2) -> {
            Integer sizeO1;
            if (Objects.isNull(o1.getLikes())) {
                sizeO1 = -2;
            } else if (o1.getLikes().isEmpty()) {
                sizeO1 = -1;
            } else {
                sizeO1 = o1.getLikes().size();
            }
            Integer sizeO2;
            if (Objects.isNull(o2.getLikes())) {
                sizeO2 = -2;
            } else if (o2.getLikes().isEmpty()) {
                sizeO2 = -1;
            } else {
                sizeO2 = o2.getLikes().size();
            }
            if (Objects.equals(sizeO1, sizeO2)) {
                return 0;
            } else if (sizeO1 < sizeO2) {
                return 1;
            } else {
                return -1;
            }
        });
        return allFilms
                .stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    public void testIfExistFilmWithId(long id) {
        filmStorage.testIfExistFilmWithId(id);
    }
}