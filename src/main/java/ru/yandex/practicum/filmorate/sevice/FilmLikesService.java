package ru.yandex.practicum.filmorate.sevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
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
        List<Film> allFilms = filmStorage.getFilms();
        int checkedCount = allFilms.size();
        if (checkedCount < count) {
            count = checkedCount;
        }
        return allFilms
                .stream()
                .sorted(Comparator.comparing(f -> f.getLikeIds().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }
}