package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.sevice.ValidationOfInputNumbers;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final InMemoryUserStorage userStorage;
    private final ValidationOfInputNumbers validationOfInputNumbers;

    @Autowired
    public UserController(InMemoryUserStorage userStorage, ValidationOfInputNumbers validationOfInputNumbers) {
        this.userStorage = userStorage;
        this.validationOfInputNumbers = validationOfInputNumbers;
    }

    @PostMapping
    public User create(@RequestBody @Valid final User user) {
        log.info("POST request for user {}", user);
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@RequestBody @Valid final User user) {
        log.info("PUT request for user {}", user);
        return userStorage.update(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("GET request - all users");
        return userStorage.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        log.info("GET request - user id={} ", id);
        validationOfInputNumbers.validateIdIsNumber(id, "User");
        long userId = Long.parseLong(id);
        validationOfInputNumbers.validatePositiveInputNumber(userId, "User");
        userStorage.testIfExistUserWithId(userId);
        return userStorage.getUser(userId);
    }
}