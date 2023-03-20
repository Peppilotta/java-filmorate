package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Set;

public interface GenreStorage {

    Genre getGenre(long id);

    Set<Genre> getGenres();

    boolean containsGenre(long id);

    long saveGenre(Genre genre);
}
