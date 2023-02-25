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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.sevice.FilmService;
import ru.yandex.practicum.filmorate.sevice.UserService;
import ru.yandex.practicum.filmorate.sevice.ValidationOfInputNumbers;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmLikeController {

    private final FilmService filmService;
    private final UserService userService;
    private final ValidationOfInputNumbers validationOfInputNumbers;

    @Autowired
    public FilmLikeController(FilmService filmService, UserService userService,
                              ValidationOfInputNumbers validationOfInputNumbers) {
        this.filmService = filmService;
        this.userService = userService;
        this.validationOfInputNumbers = validationOfInputNumbers;
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
        validationOfInputNumbers.validateIdIsNumber(id, "Film");
        validationOfInputNumbers.validateIdIsNumber(userId, "User");
        long filmId = Long.parseLong(id);
        long userNumber = Long.parseLong(userId);
        validationOfInputNumbers.validatePositiveInputNumber(filmId, "Film");
        validationOfInputNumbers.validatePositiveInputNumber(userNumber, "User");
        filmService.testIfExistFilmWithId(filmId);
        userService.testIfExistUserWithId(userNumber);
        return new Long[]{filmId, userNumber};
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") String count) {
        log.info("GET request - popular films, highest {}", count);
        validationOfInputNumbers.validateIdIsNumber(count, "Count");
        int countNumber = Integer.parseInt(count);
        validationOfInputNumbers.validatePositiveInputNumber(countNumber, "Count");
        return filmService.getCountFavoriteFilms(countNumber);
    }
}
