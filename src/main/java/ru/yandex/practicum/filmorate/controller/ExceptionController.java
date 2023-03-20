package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.FriendsDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.GenreDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.LikesDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.MpaDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UserDoesNotExistException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private final Map<String, String> messages = new HashMap<>(Map.of(
            "FilmDoesNotExistException", "film id exception",
            "UserDoesNotExistException", "user id exception",
            "GenreDoesNotExistException", "genre id exception",
            "MpaDoesNotExistException", "MPA id exception",
            "LikesDoesNotExistException", "likes exception",
            "FriendsDoesNotExistException", "friends exception"
    ));

    @ExceptionHandler({FilmDoesNotExistException.class,
            UserDoesNotExistException.class,
            GenreDoesNotExistException.class,
            MpaDoesNotExistException.class,
            LikesDoesNotExistException.class,
            FriendsDoesNotExistException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongFilmId(final RuntimeException e) {
        String className = e.getClass().getSimpleName();
        log.error(e.getMessage(), e);
        return Map.of(ERROR, messages.get(className),
                MESSAGE, e.getMessage());
    }
}