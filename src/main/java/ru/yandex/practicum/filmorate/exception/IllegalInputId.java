package ru.yandex.practicum.filmorate.exception;

public class IllegalInputId extends RuntimeException {
    public IllegalInputId(String message) {
        super(message);
    }
}