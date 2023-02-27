package ru.yandex.practicum.filmorate.sevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        fillUserName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        fillUserName(user);
        return userStorage.update(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUser(long id) {
        return userStorage.getUser(id);
    }

    private void fillUserName(User user) {
        String name = user.getName();
        if (Objects.isNull(name) || name.trim().length() == 0) {
            user.setName(user.getLogin());
        }
    }
}