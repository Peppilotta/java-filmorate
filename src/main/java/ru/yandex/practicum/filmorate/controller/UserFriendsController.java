package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.IdIsNotNumber;
import ru.yandex.practicum.filmorate.exception.IllegalInputId;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.sevice.UserFriendsService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserFriendsController {

    private final UserFriendsService userService;

    @Autowired
    public UserFriendsController(UserFriendsService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable String id, @PathVariable String friendId) {
        log.info("PUT request for add friend with id={} to user with id={}", friendId, id);
        Long[] ids = testForAddOrDeleteFriend(id, friendId);
        userService.addFriend(ids[0], ids[1]);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable String id, @PathVariable String friendId) {
        log.info("Delete request for remove friend with id={} for user with id={}", friendId, id);
        Long[] ids = testForAddOrDeleteFriend(id, friendId);
        userService.deleteFriend(ids[0], ids[1]);
    }

    private Long[] testForAddOrDeleteFriend(String id, String friendId) {
        long user1Id = validateIdIsNumber(id);
        long user2Id = validateIdIsNumber(friendId);
        validatePositive(user1Id);
        validatePositive(user2Id);
        return new Long[]{user1Id, user2Id};
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable String id) {
        log.info("Get request for list friends of user with id={}", id);
        long userId = validateIdIsNumber(id);
        validatePositive(userId);
        return userService.getFriendIds(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable String id, @PathVariable String otherId) {
        log.info("Get request for common list of friends of user with id={} and user with id={} "
                , id, otherId);
        Long[] ids = testForAddOrDeleteFriend(id, otherId);
        return userService.getCommonFriends(ids[0], ids[1]);
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