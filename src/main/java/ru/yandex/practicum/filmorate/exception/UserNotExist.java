package ru.yandex.practicum.filmorate.exception;

public class UserNotExist extends RuntimeException {
    public UserNotExist(String message) {
        super(message);
    }
}