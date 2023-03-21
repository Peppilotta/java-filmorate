package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ItemDoesNotExistException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(ItemDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleWrongFilmId(final RuntimeException e) {
        log.error(e.getMessage(), e);
        return Map.of(ERROR, "wrong id",
                MESSAGE, e.getMessage());
    }
}