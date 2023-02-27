package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.IdIsNotNumber;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.sevice.FilmLikesService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmLikeController {

    private final FilmLikesService filmService;

    @Autowired
    public FilmLikeController(FilmLikesService filmService) {
        this.filmService = filmService;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable String id, @PathVariable String userId) {
        log.info("PUT request for like for film id={} from user id={}", id, userId);
        Long[] numbers = testForAddOrDeleteLike(id, userId);
        filmService.addLike(numbers[0], numbers[1]);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable String id, @PathVariable String userId) {
        log.info("DELETE request for like for film id={} from user id={}", id, userId);
        Long[] numbers = testForAddOrDeleteLike(id, userId);
        filmService.deleteLike(numbers[0], numbers[1]);
    }

    private Long[] testForAddOrDeleteLike(String id, String userId) {
        long filmId = validateIdIsNumber(id, "Film");
        long userNumber = validateIdIsNumber(userId, "User");
        validatePositive(filmId, "Film");
        validatePositive(userNumber, "User");
        return new Long[]{filmId, userNumber};
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") String count) {
        log.info("GET request - popular films, highest {}", count);
        int countNumber = (int) validateIdIsNumber(count, "Count");
        validatePositive(countNumber, "Count");
        return filmService.getCountFavoriteFilms(countNumber);
    }

    private void validatePositive(long id, String item) {
        if (id < 0) {
            throw new IllegalInputId(item + " id is not positive");
        }
    }

    private long validateIdIsNumber(String id, String item) throws IdIsNotNumber {
        try {
            return Long.parseLong(id);
        } catch (IdIsNotNumber e) {
            throw new IdIsNotNumber(item + " id must be a number");
        }
    }
}