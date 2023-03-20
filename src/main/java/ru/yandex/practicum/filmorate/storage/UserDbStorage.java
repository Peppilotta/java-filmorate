package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Friend;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String GET_USERS = "SELECT * FROM Users ORDER BY user_id ";

    private static final String GET_USER = "SELECT * FROM Users WHERE user_id=?";

    private static final String GET_USER_ID = "SELECT user_id FROM Users WHERE user_id=?";

    private static final String UPDATE_USER =
            "UPDATE Users SET user_name=?, login=?, email=?, birthday=? where user_id=? ";

    private static final String GET_USER_FRIENDS = "SELECT * FROM Friends WHERE user_id=? ORDER BY friend_id ";

    private static final String GET_USER_FRIENDS_AS_USERS =
            "SELECT * FROM users WHERE user_id IN " +
                    "(SELECT friend_id FROM friends WHERE user_id=? ORDER BY friend_id) ORDER BY user_id";

    private static final String GET_COMMON_FRIENDS_AS_USERS =
            "SELECT * FROM users WHERE user_id IN (SELECT friend_id FROM friends WHERE user_id=? AND friend_id IN " +
                    "(SELECT friend_id FROM friends WHERE user_id=?) ORDER BY friend_id) ORDER BY user_id";

    private static final String DELETE_FRIENDSHIP = "DELETE FROM friends WHERE user_id=? AND friend_id=?";

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        long userId = saveUser(user);
        Set<Friend> friends = user.getFriends();
        if (!friends.isEmpty()) {
            updateFriends(friends, userId);
        }
        return getUser(userId);
    }

    @Override
    public User update(User user) {
        long userId = user.getId();
        jdbcTemplate.update(UPDATE_USER, user.getName(), user.getLogin(),
                user.getEmail(), user.getBirthday(), userId);
        deleteFriends(getFriendships(userId), userId);
        updateFriends(user.getFriends(), userId);
        return getUser(userId);
    }

    @Override
    public List<User> getUsers() {
        return getSomeUsers(getUsersOnly());
    }

    @Override
    public User getUser(long id) {
        User userOut = getUserOnly(id);
        userOut.setFriends(getFriendships(id));
        return userOut;
    }

    @Override
    public User addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new UserDoesNotExistException(" ids are equals. ");
        }
        User user = getUser(userId);
        Friend userToOtherUser = Friend.builder()
                .friendId(friendId)
                .approval(false)
                .build();
        Set<Friend> userFriends = user.getFriends();
        userFriends.add(userToOtherUser);
        update(user);
        return user;
    }

    @Override
    public User deleteFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new UserDoesNotExistException(" ids are equals. ");
        }
        User user = getUser(userId);
        Set<Friend> friends = user.getFriends();
        for (Friend friend : friends) {
            if (Objects.equals(friend.getFriendId(), friendId)) {
                friends.remove(friend);
            }
        }
        update(user);
        return user;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        if (userId == otherUserId) {
            throw new UserDoesNotExistException(" ids are equals. ");
        }
        return getSomeUsers(getCommonFriendsAsUser(userId, otherUserId));
    }

    public List<User> getSomeUsers(List<User> users) {
        if (users.isEmpty()) {
            return new ArrayList<>();
        }
        users.forEach(f -> {
            long id = f.getId();
            f.setFriends(getFriendships(id));
        });
        return users;
    }

    @Override
    public List<User> getFriends(long userId) {
        return getSomeUsers(getFriendsAsUser(userId));
    }

    @Override
    public boolean containsUser(long userId) {
        if (userId < 0) {
            throw new UserDoesNotExistException("User id mast be positive");
        }
        try {
            SqlRowSet resultSet = jdbcTemplate.queryForRowSet(GET_USER_ID, userId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            throw new UserDoesNotExistException("User with id=" + userId + " not exist. ");
        }
    }

    private long saveUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        return simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue();
    }

    private long saveFriendship(Friendship friendship) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("Friends")
                .usingGeneratedKeyColumns("friendship_id");
        return simpleJdbcInsert.executeAndReturnKey(friendship.toMap()).longValue();
    }

    private User mapRowToUser(ResultSet rs, long rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .login(rs.getString("login"))
                .name(rs.getString("user_name"))
                .email(rs.getString("email"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    private Friend mapRowToFriendship(ResultSet rs, long rowNum) throws SQLException {
        return Friend.builder()
                .friendId(rs.getLong("friend_id"))
                .approval(rs.getBoolean("approval"))
                .build();
    }

    private Set<Friend> getFriendships(long userId) {
        return new HashSet<>
                (jdbcTemplate.query(GET_USER_FRIENDS, this::mapRowToFriendship, userId));
    }

    private List<User> getUsersOnly() {
        return new ArrayList<>(jdbcTemplate.query(GET_USERS, this::mapRowToUser));
    }

    private List<User> getFriendsAsUser(long userId) {
        return new ArrayList<>(jdbcTemplate.query(GET_USER_FRIENDS_AS_USERS, this::mapRowToUser, userId));
    }

    private List<User> getCommonFriendsAsUser(long userId, long otherUserId) {
        return new ArrayList<>
                (jdbcTemplate.query(GET_COMMON_FRIENDS_AS_USERS, this::mapRowToUser, userId, otherUserId));
    }

    private User getUserOnly(long userId) {
        try {
            return jdbcTemplate.queryForObject(GET_USER, this::mapRowToUser, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new UserDoesNotExistException("User with id=" + userId + " not exist. ");
        }
    }

    private boolean updateFriends(Set<Friend> friends, long userId) {
        long count = 0;
        for (Friend friend : friends) {
            count += saveFriendship(Friendship.builder()
                    .userId(userId)
                    .friendId(friend.getFriendId())
                    .approval(friend.isApproval())
                    .build());
        }
        return count == friends.size();
    }

    private boolean deleteFriends(Set<Friend> friends, long userId) {
        long count = 0;
        for (Friend friend : friends) {
            count += jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friend.getFriendId());
        }
        return count == friends.size();
    }
}

