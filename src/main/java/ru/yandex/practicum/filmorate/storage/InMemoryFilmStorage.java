package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long meter = 1L;

    @Override
    public Film create(Film film) {
        film.setId(meter);
        films.put(meter, film);
        meter++;
        return film;
    }

    @Override
    public Film update(Film film) {
        long id = film.getId();
        if (!containsFilm(id)) {
            throw new FilmDoesNotExistException("Film with id=" + id + " not exist. ");
        }
        films.put(id, film);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(long id) {
        if (!containsFilm(id)) {
            throw new FilmDoesNotExistException("Film with id=" + id + " not exist. ");
        }
        return films.get(id);
    }

    @Override
    public boolean containsFilm(long id) {
        return films.containsKey(id);
    }
}