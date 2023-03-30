package ru.yandex.practicum.filmorate.exception;

public class ItemDoesNotExistException extends RuntimeException {
    public ItemDoesNotExistException(String message) {
        super(message);
    }
}