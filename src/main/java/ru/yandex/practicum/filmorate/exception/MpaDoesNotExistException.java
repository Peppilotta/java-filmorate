package ru.yandex.practicum.filmorate.exception;

public class MpaDoesNotExistException extends RuntimeException {
    public MpaDoesNotExistException(String message) {
        super(message);
    }
}