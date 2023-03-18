package ru.yandex.practicum.filmorate.exception;

public class GenreDoesNotExistException extends RuntimeException {
    public GenreDoesNotExistException(String message) {
        super(message);
    }
}