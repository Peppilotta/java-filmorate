package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String GET_USERS = "SELECT * FROM users ORDER BY user_id ";

    private static final String GET_USER = "SELECT * FROM users WHERE user_id=?";

    private static final String GET_USER_ID = "SELECT user_id FROM users WHERE user_id=?";

    private static final String UPDATE_USER =
            "UPDATE users SET user_name=?, login=?, email=?, birthday=? WHERE user_id=? ";

    private static final String GET_USER_FRIENDS_AS_USERS =
            "SELECT * FROM users WHERE user_id IN " +
                    "(SELECT friend_id FROM friends WHERE user_id=? ORDER BY friend_id) ORDER BY user_id";

    private static final String GET_COMMON_FRIENDS_AS_USERS =
            "SELECT * FROM users " +
                    "WHERE user_id IN " +
                    "(SELECT friend_id FROM friends " +
                    "WHERE user_id=? AND friend_id IN " +
                    "(SELECT friend_id FROM friends " +
                    "WHERE user_id=?) " +
                    "ORDER BY friend_id) " +
                    "ORDER BY user_id";

    private static final String INSERT_FRIEND = "INSERT INTO friends (user_id, friend_id, approval) VALUES (?,?,?)";

    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE user_id=? AND friend_id=?";

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        long userId = saveUser(user);
        return getUser(userId);
    }

    @Override
    public User update(User user) {
        long userId = user.getId();
        jdbcTemplate.update(UPDATE_USER, user.getName(), user.getLogin(),
                user.getEmail(), user.getBirthday(), userId);
        return getUser(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(jdbcTemplate.query(GET_USERS, this::mapRowToUser));
    }

    @Override
    public User getUser(long id) {
        return jdbcTemplate.queryForObject(GET_USER, this::mapRowToUser, id);
    }

    @Override
    public User addFriend(long userId, long friendId) {
        jdbcTemplate.update(INSERT_FRIEND, userId, friendId, false);
        return getUser(friendId);
    }

    @Override
    public User deleteFriend(long userId, long friendId) {
        jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
        return getUser(friendId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        return new ArrayList<>(jdbcTemplate
                .query(GET_COMMON_FRIENDS_AS_USERS, this::mapRowToUser, userId, otherUserId));
    }

    @Override
    public List<User> getFriends(long userId) {
        return new ArrayList<>(jdbcTemplate.query(GET_USER_FRIENDS_AS_USERS, this::mapRowToUser, userId));
    }

    @Override
    public boolean containsUser(long userId) {
        return jdbcTemplate.queryForRowSet(GET_USER_ID, userId).next();
    }

    private long saveUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        return simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue();
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
}