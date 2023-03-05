package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.User;

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
        user.setId(meter);
        users.put(meter, user);
        meter++;
        return user;
    }

    @Override
    public User update(User user) {
        long id = user.getId();
        if (!containsUser(id)) {
            return null;
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
        if (!containsUser(id)) {
            return null;
        }
        return users.get(id);
    }

    @Override
    public boolean containsUser(long id) {
        if (Objects.isNull(users.get(id))) {
            throw new UserDoesNotExistException("User with id=" + id + NOT_EXIST);
        }
        return true;
    }
}