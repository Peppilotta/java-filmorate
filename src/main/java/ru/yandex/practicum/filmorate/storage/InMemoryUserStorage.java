package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotExist;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class InMemoryUserStorage implements UserStorage {
    private static final String NOT_EXIST = " not exist. ";
    private final Map<Long, User> users = new HashMap<>();
    private long meter = 1L;

    @Override
    public User create(User user) {
        fillUserName(user);
        user.setId(meter);
        users.put(meter, user);
        meter++;
        return user;
    }

    @Override
    public User update(User user) {
        fillUserName(user);
        long id = user.getId();
        if (!users.containsKey(id)) {
            throw new ValidationException("Wrong user id.");
        }
        users.put(id, user);
        return user;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(long id) {
        if (!users.containsKey(id)) {
            return null;
        }
        return users.get(id);
    }

    public void fillUserName(User user) {
        String name = user.getName();
        if (Objects.isNull(name) || name.trim().length() == 0) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public void testIfExistUserWithId(long id) {
        if (Objects.isNull(users.get(id))) {
            throw new UserNotExist("User with " + id + NOT_EXIST);
        }
    }
}