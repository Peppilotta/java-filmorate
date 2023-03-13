package ru.yandex.practicum.filmorate.sevice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Friendship;
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
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
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
        User user = userStorage.getUser(userId);
        Set<Friendship> userFriends = user.getFriendships();
        User otherUser = userStorage.getUser(friendId);
        Set<Friendship> otherUserFriends = otherUser.getFriendships();
        userFriends.add(new Friendship(friendId,false));
        otherUserFriends.add(new Friendship(userId,false));
        user.setFriendships(userFriends);
        otherUser.setFriendships(otherUserFriends);
        userStorage.update(user);
        userStorage.update(otherUser);
    }

    public void deleteFriend(long userId, long friendId) {
        log.info("Remove friend with id={} for user with id={}", friendId, userId);
        userStorage.containsUser(userId);
        userStorage.containsUser(friendId);
        User user = userStorage.getUser(userId);
        User otherUser = userStorage.getUser(friendId);
        Set<Friendship> userFriends = user.getFriendships();
        Set<Friendship> otherUserFriends = otherUser.getFriendships();
        user.setFriendships(
                userFriends.stream()
                .filter(f -> !Objects.equals(f.getFriendId(),friendId))
                .collect(Collectors.toSet()));
        otherUser.setFriendships(
                otherUserFriends.stream()
                .filter(f -> !Objects.equals(f.getFriendId(),userId))
                .collect(Collectors.toSet()));
        userStorage.update(user);
        userStorage.update(otherUser);
    }

    public List<User> getFriends(long userId) {
        log.info("Get list friends of user with id={}", userId);
        userStorage.containsUser(userId);
        Set<Long> userFriends = collectFriendsIds(userId);
        List<User> allUsers = userStorage.getUsers();
        if (userFriends.isEmpty()) {
            return new ArrayList<>();
        }
        return allUsers.stream()
                .filter(f -> userFriends.contains(f.getId()))
                .collect(Collectors.toList());
    }

    private Set<Long> collectFriendsIds(long  id) {
        return userStorage.getUser(id).getFriendships()
                .stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        log.info("Get request for common list of friends of user with id={} and user with id={} "
                , userId, otherUserId);
        userStorage.containsUser(userId);
        userStorage.containsUser(otherUserId);
        Set<Long> cross = collectFriendsIds(userId)
                .stream()
                .filter(collectFriendsIds(otherUserId)::contains)
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
        if (Objects.isNull(name) || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }
}