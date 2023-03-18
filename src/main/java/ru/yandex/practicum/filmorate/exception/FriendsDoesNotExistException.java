package ru.yandex.practicum.filmorate.exception;

public class FriendsDoesNotExistException extends RuntimeException {

    public FriendsDoesNotExistException(String message) {
        super(message);
    }
}