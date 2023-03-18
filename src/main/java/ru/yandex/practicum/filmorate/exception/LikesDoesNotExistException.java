package ru.yandex.practicum.filmorate.exception;

public class LikesDoesNotExistException extends RuntimeException {

    public LikesDoesNotExistException(String message) {
        super(message);
    }
}