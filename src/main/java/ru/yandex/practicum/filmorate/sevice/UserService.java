package ru.yandex.practicum.filmorate.sevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        User user1 = userStorage.getUser(userId);
        Set<Long> user1Friends = user1.getFriends();
        if (Objects.isNull(user1Friends)) {
            user1Friends = new HashSet<>();
        }
        User user2 = userStorage.getUser(friendId);
        Set<Long> user2Friends = user2.getFriends();
        if (Objects.isNull(user2Friends)) {
            user2Friends = new HashSet<>();
        }
        user1Friends.add(friendId);
        user2Friends.add(userId);
        user1.setFriends(user1Friends);
        user2.setFriends(user2Friends);
    }

    public void deleteFriend(long userId, long friendId) {
        Set<Long> user1Friends = userStorage.getUser(userId).getFriends();
        Set<Long> user2Friends = userStorage.getUser(friendId).getFriends();
        user1Friends.remove(friendId);
        user2Friends.remove(userId);
    }

    public List<User> getFriends(long userId) {
        Set<Long> userFriends = userStorage.getUser(userId).getFriends();
        List<User> allUsers = userStorage.getUsers();
        if (userFriends.isEmpty()) {
            return new ArrayList<>();
        }
        return allUsers.stream()
                .filter(f -> userFriends.contains(f.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long user1Id, long user2Id) {
        Set<Long> user1Friends = userStorage.getUser(user1Id).getFriends();
        if (Objects.isNull(user1Friends)) {
            return new ArrayList<>();
        }
        Set<Long> user2Friends = userStorage.getUser(user2Id).getFriends();
        if (Objects.isNull(user2Friends)) {
            return new ArrayList<>();
        }
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

    public void testIfExistUserWithId(long id) {
        userStorage.testIfExistUserWithId(id);
    }
}