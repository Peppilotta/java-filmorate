package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.FriendsDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.GenreDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.LikesDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.MpaDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.UserDoesNotExistException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(FilmDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongFilmId(final RuntimeException e) {
        log.error("Film with this id not exist.", e);
        return Map.of(ERROR, "film id exception",
                MESSAGE, e.getMessage());
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongUserId(final RuntimeException e) {
        log.error("User with this id not exist.", e);
        return Map.of(ERROR, "user id exception",
                MESSAGE, e.getMessage());
    }

    @ExceptionHandler(GenreDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongGenreId(final RuntimeException e) {
        log.error("Genre with this id not exist.", e);
        return Map.of(ERROR, "genre id exception",
                MESSAGE, e.getMessage());
    }

    @ExceptionHandler(MpaDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongMpaId(final RuntimeException e) {
        log.error("Mpa with this id not exist.", e);
        return Map.of(ERROR, "MPA id exception",
                MESSAGE, e.getMessage());
    }

    @ExceptionHandler(LikesDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEmptyLikes(final RuntimeException e) {
        log.error("Likes is absent.", e);
        return Map.of(ERROR, "likes exception",
                MESSAGE, e.getMessage());
    }

    @ExceptionHandler(FriendsDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEmptyFriends(final RuntimeException e) {
        log.error("Friends is absent.", e);
        return Map.of(ERROR, "friends exception",
                MESSAGE, e.getMessage());
    }
}