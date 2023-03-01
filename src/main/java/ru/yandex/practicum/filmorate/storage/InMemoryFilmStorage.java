package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            return null;
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
            return null;
        }
        return films.get(id);
    }

    @Override
    public boolean containsFilm(long id) {
        if (Objects.isNull(films.get(id))) {
            throw new FilmDoesNotExistException("Film with id=" + id + " not exist. ");
        }
        return true;
    }
}