package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationExceptions;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long meter = 1L;

    @PostMapping("/users")
    public User create(@RequestBody @Valid final User user) {
        validate(user);
        user.setId(meter);
        users.put(meter, user);
        meter++;
        log.info("POST request for user {}", user);
        return user;
    }

    @PutMapping("/users")
    public User update(@RequestBody @Valid final User user) {
        validate(user);
        long id = user.getId();
        log.info("PUT request for user {}", user);
        if (users.containsKey(id)) {
            users.put(id, user);
            return user;
        } else {
            throw new ValidationException("Wrong user id.");
        }
    }

    @GetMapping("/users")
    public List<User> getAll() {
        log.info("GET request");
        return new ArrayList<>(users.values());
    }

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