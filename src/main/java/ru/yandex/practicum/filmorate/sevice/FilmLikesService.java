package ru.yandex.practicum.filmorate.sevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmLikesService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmLikesService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(long filmId, long userId) {
        filmStorage.containsFilm(filmId);
        userStorage.containsUser(userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLikeIds();
        likes.add(userId);
        film.setLikeIds(likes);
    }

    public void deleteLike(long filmId, long userId) {
        filmStorage.containsFilm(filmId);
        userStorage.containsUser(userId);
        Set<Long> likes = filmStorage.getFilm(filmId).getLikeIds();
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
            int sizeO1;
            if (o1.getLikeIds().isEmpty()) {
                sizeO1 = -1;
            } else {
                sizeO1 = o1.getLikeIds().size();
            }
            int sizeO2;
            if (o2.getLikeIds().isEmpty()) {
                sizeO2 = -1;
            } else {
                sizeO2 = o2.getLikeIds().size();
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
}