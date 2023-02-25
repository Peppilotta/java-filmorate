package ru.yandex.practicum.filmorate.exception;

public class IdIsNotNumber extends RuntimeException {
    public IdIsNotNumber(String message) {
        super(message);
    }
}