package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(User user);

    List<User> getUsers();

    User getUser(long id);

    boolean containsUser(long id);

    boolean addFriend(long userId, long friendId);

    boolean deleteFriend(long userId, long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherUserId);
}