package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Set;

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