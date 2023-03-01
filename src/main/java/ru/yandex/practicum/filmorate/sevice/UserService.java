package ru.yandex.practicum.filmorate.sevice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        fillUserName(user);
        return userStorage.update(user);
    }

    public List<User> getUsers() {
        log.info("GET request - all users");
        return userStorage.getUsers();
    }

    public User getUser(long id) {
        log.info("GET request - user id={} ", id);
        return userStorage.getUser(id);
    }

    public void addFriend(long userId, long friendId) {
        log.info("Add friend with id={} to user with id={}", friendId, userId);
        userStorage.containsUser(userId);
        userStorage.containsUser(friendId);
        User user1 = userStorage.getUser(userId);
        Set<Long> user1Friends = user1.getFriendIds();
        User user2 = userStorage.getUser(friendId);
        Set<Long> user2Friends = user2.getFriendIds();
        user1Friends.add(friendId);
        user2Friends.add(userId);
        user1.setFriendIds(user1Friends);
        user2.setFriendIds(user2Friends);
    }

    public void deleteFriend(long userId, long friendId) {
        log.info("Remove friend with id={} for user with id={}", friendId, userId);
        userStorage.containsUser(userId);
        userStorage.containsUser(friendId);
        Set<Long> user1Friends = userStorage.getUser(userId).getFriendIds();
        Set<Long> user2Friends = userStorage.getUser(friendId).getFriendIds();
        user1Friends.remove(friendId);
        user2Friends.remove(userId);
    }

    public List<User> getFriendIds(long userId) {
        log.info("Get list friends of user with id={}", userId);
        userStorage.containsUser(userId);
        Set<Long> userFriends = userStorage.getUser(userId).getFriendIds();
        List<User> allUsers = userStorage.getUsers();
        if (userFriends.isEmpty()) {
            return new ArrayList<>();
        }
        return allUsers.stream()
                .filter(f -> userFriends.contains(f.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long user1Id, long user2Id) {
        log.info("Get request for common list of friends of user with id={} and user with id={} "
                , user1Id, user2Id);
        userStorage.containsUser(user1Id);
        userStorage.containsUser(user2Id);
        Set<Long> user1Friends = userStorage.getUser(user1Id).getFriendIds();
        Set<Long> user2Friends = userStorage.getUser(user2Id).getFriendIds();
        Set<Long> cross = user1Friends
                .stream()
                .filter(user2Friends::contains)
                .collect(Collectors.toSet());
        if (cross.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> allUsers = userStorage.getUsers();
        return allUsers.stream()
                .filter(f -> cross.contains(f.getId()))
                .collect(Collectors.toList());
    }

    private void fillUserName(User user) {
        String name = user.getName();
        if (Objects.isNull(name) || name.trim().length() == 0) {
            user.setName(user.getLogin());
        }
    }
}