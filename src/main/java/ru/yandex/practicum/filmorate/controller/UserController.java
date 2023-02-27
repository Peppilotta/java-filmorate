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
import ru.yandex.practicum.filmorate.exception.IdIsNotNumber;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.sevice.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody @Valid final User user) {
        log.info("POST request for user {}", user);
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody @Valid final User user) {
        log.info("PUT request for user {}", user);
        return userService.update(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("GET request - all users");
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        log.info("GET request - user id={} ", id);
        long userId = validateIdIsNumber(id);
        validatePositive(userId);
        return userService.getUser(userId);
    }

    private void validatePositive(long id) {
        if (id < 0) {
            throw new IllegalInputId("User id is not positive");
        }
    }

    private long validateIdIsNumber(String id) throws IdIsNotNumber {
        try {
            return Long.parseLong(id);
        } catch (IdIsNotNumber e) {
            throw new IdIsNotNumber("User id must be a number");
        }
    }
}