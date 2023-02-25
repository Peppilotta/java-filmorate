package ru.yandex.practicum.filmorate.sevice;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IdIsNotNumber;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;

@Service
public class ValidationOfInputNumbers {

    public void validatePositiveInputNumber(long id, String item) {
        if (id < 0) {
            throw new IllegalInputId(item + " id is not positive");
        }
    }

    public void validateIdIsNumber(String id, String item) throws IdIsNotNumber {
        try {
            Long filmId = Long.parseLong(id);
        } catch (IdIsNotNumber e) {
            throw new IdIsNotNumber(item + " id must be a number");
        }
    }
}