package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class UserController extends ProgenitorController<User> {

    @PostMapping("/users")
    @Override
    public User create(@RequestBody @Valid final User user) {
        log.info("POST request for user {}", user);
        return super.create(user);
    }

    @PutMapping("/users")
    @Override
    public User update(@RequestBody @Valid final User user) {
        log.info("PUT request for user {}", user);
        User real = super.update(user);
        if (Objects.isNull(real)) {
            throw new ValidationExceptions("Wrong user id.");
        }
        return real;
    }

    @GetMapping("/users")
    @Override
    public List<User> getAll() {
        log.info("GET request");
        return new ArrayList<>(super.getAll());
    }

    @Override
    public void validate(User user) {
        String name = user.getName();
        if (Objects.isNull(name) || name.trim().length() == 0) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationExceptions("Bad birthday");
        }
    }
}