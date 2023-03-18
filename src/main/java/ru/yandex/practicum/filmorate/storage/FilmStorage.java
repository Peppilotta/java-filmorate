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

    List<Film> getFavoriteFilms(int count);

    Film getFilm(long id);

    Genre getGenre(long id);

    boolean addLike(long filmId, long userId);

    boolean deleteLike(long filmId, long userId);

    Set<Genre> getGenres();

    Mpa getMpa(long id);

    Set<Mpa> getMpas();

    boolean containsFilm(long id);
}