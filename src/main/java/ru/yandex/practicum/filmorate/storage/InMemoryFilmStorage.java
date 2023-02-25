package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotExist;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final String NOT_EXIST = " not exist. ";
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate filmsStart = LocalDate.parse("28.12.1895",
            DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    private long meter = 1L;

    @Override
    public Film create(Film film) {
        validateDateCreation(film);
        film.setId(meter);
        films.put(meter, film);
        meter++;
        return film;
    }

    @Override
    public Film update(Film film) {
        validateDateCreation(film);
        long id = film.getId();
        if (!films.containsKey(id)) {
            throw new ValidationException("Wrong user id.");
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
        if (!films.containsKey(id)) {
            return null;
        }
        return films.get(id);
    }

    public void validateDateCreation(Film film) {
        if (film.getReleaseDate().isBefore(filmsStart)) {
            throw new ValidationExceptions("Bad date");
        }
    }

    @Override
    public void testIfExistFilmWithId(long id) {
        if (Objects.isNull(films.get(id))) {
            throw new FilmNotExist("Film with " + id + NOT_EXIST);
        }
    }
}