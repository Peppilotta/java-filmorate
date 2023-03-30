package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ItemDoesNotExistException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        log.info("Create request for user {}", user);
        fillUserName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        log.info("Update request for user {}", user);
        containsUser(user.getId());
        fillUserName(user);
        return userStorage.update(user);
    }

    public List<User> getUsers() {
        log.info("GET request - all users");
        return userStorage.getUsers();
    }

    public User getUser(long id) {
        log.info("GET request - user id={} ", id);
        containsUser(id);
        return userStorage.getUser(id);
    }

    public User addFriend(long userId, long friendId) {
        log.info("Add friend with id={} to user with id={}", friendId, userId);
        if (userId == friendId) {
            throw new ItemDoesNotExistException(" ids are equals. ");
        }
        containsUser(userId);
        containsUser(friendId);
        return userStorage.addFriend(userId, friendId);
    }

    public User deleteFriend(long userId, long friendId) {
        log.info("Remove friend with id={} for user with id={}", friendId, userId);
        if (userId == friendId) {
            throw new ItemDoesNotExistException(" ids are equals. ");
        }
        containsUser(userId);
        containsUser(friendId);
        return userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        log.info("Get list friends of user with id={}", userId);
        containsUser(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        log.info("Get request for common list of friends of user with id={} and user with id={} "
                , userId, otherUserId);
        containsUser(userId);
        containsUser(otherUserId);
        return userStorage.getCommonFriends(userId, otherUserId);
    }

    private void fillUserName(User user) {
        String name = user.getName();
        if (Objects.isNull(name) || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void containsUser(long id) {
        if (!userStorage.containsUser(id)) {
            throw new ItemDoesNotExistException("User with id=" + id + " not exist. ");
        }
    }
}