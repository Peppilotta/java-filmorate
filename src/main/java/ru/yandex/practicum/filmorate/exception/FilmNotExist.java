package ru.yandex.practicum.filmorate.exception;

public class FilmNotExist extends RuntimeException {
    public FilmNotExist(String message) {
        super(message);
    }
}
