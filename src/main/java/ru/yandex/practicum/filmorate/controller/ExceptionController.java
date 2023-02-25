package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.exception.FilmNotExist;
import ru.yandex.practicum.filmorate.exception.IdIsNotNumber;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.exception.UserNotExist;

import java.util.Map;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(FilmNotExist.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongFilmId(final RuntimeException e) {
        return Map.of("error", "film id exception",
                "errorMessage", e.getMessage());
    }

    @ExceptionHandler({IllegalInputId.class, IdIsNotNumber.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalUserId(final RuntimeException e) {
        return Map.of("error", "wrong number",
                "errorMessage", e.getMessage());
    }

    @ExceptionHandler(UserNotExist.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongUserId(final RuntimeException e) {
        return Map.of("error", "user id exception",
                "errorMessage", e.getMessage());
    }
}