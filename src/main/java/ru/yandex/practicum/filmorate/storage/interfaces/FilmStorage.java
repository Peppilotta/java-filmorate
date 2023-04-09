package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> getFilms();

    List<Film> getTheMostPopularFilms(int count);

    Film getFilm(long id);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    boolean containsFilm(long id);
}