package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.exception.FilmNotExist;
import ru.yandex.practicum.filmorate.exception.UserNotExist;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(FilmNotExist.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongFilmId(final RuntimeException e) {
        log.error("Film with this id not exist.", e);
        return Map.of("error", "film id exception",
                "message", e.getMessage());
    }

    @ExceptionHandler(UserNotExist.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongUserId(final RuntimeException e) {
        log.error("User with this id not exist.", e);
        return Map.of("error", "user id exception",
                "message", e.getMessage());
    }
}