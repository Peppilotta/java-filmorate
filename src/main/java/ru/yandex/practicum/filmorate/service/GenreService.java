package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ItemDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Set;

@Service
@Slf4j
public class GenreService {

    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Set<Genre> getGenres() {
        log.info("GET request - all genres");
        return genreStorage.getGenres();
    }

    public Genre getGenre(int id) {
        log.info("GET request - genre with id={}", id);
        containsGenre(id);
        return genreStorage.getGenre(id);
    }

    private void containsGenre(long id) {
        if (!genreStorage.containsGenre(id)) {
            throw new ItemDoesNotExistException("Genre with id=" + id + " not exist. ");
        }
    }
}