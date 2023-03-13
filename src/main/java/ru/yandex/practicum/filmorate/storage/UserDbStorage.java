package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String GET_ONE_USER_FROM_DB = "select * from Users where user_id=?";
    private static final String PUT_ONE_USER_TO_DB = "insert into Users (user_name," +
            "login, " +
            "email, " +
            "birthday) values (?,?,?,?)";

    private static final String UPDATE_ONE_USER_TO_DB = "update Users set user_name=?, " +
            "login=?, " +
            "email=?, " +
            "birthday=? where user_id=? ";
    private static final String GET_FRIENDS_OF_ONE_USER_FROM_DB = "select * from Likes where user_id=?";
    private static final String PUT_FRIENDSHIP_OF_ONE_USER_TO_DB =
            "insert into Friends (user_id, friend_id, approval) values (?, ?, ?)";
    private static final String DELETE_FRIENDS_OF_ONE_USER_FROM_DB = "delete from Likes where user_id=?";
    private static final String GET_USER_MAX_ID = "SELECT MAX(user_id) as max_id from Users";

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        jdbcTemplate.update(PUT_ONE_USER_TO_DB,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday());
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(GET_USER_MAX_ID);
        long userId = sqlRowSet.getLong("max_id");
        Set<Friendship> friendships = user.getFriendships();
        if (!friendships.isEmpty()) {
            addFriendshipsToDb(userId, friendships);
        }
        return getUser(userId);
    }

    public void addFriendshipToDb(long userId, Friendship friendship) {
        jdbcTemplate.update(PUT_FRIENDSHIP_OF_ONE_USER_TO_DB,
                userId,
                friendship.getFriendId(),
                friendship.isApproval());
    }

    public void addFriendshipsToDb(long userId, Set<Friendship> friends) {
        for (Friendship friendship : friends) {
            addFriendshipToDb(userId, friendship);
        }
    }

    @Override
    public User update(User user) {
        long userId = user.getId();
        if (containsUser(userId)) {
            jdbcTemplate.update(UPDATE_ONE_USER_TO_DB,
                    user.getName(),
                    user.getLogin(),
                    user.getEmail(),
                    user.getBirthday(),
                    userId);
            int key = jdbcTemplate.update(DELETE_FRIENDS_OF_ONE_USER_FROM_DB, userId);
            addFriendshipsToDb(userId, user.getFriendships());
            return getUser(userId);
        }
        return null;
    }

    public User mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        User user = User.builder()
                .id(resultSet.getLong("user_id"))
                .name(resultSet.getString("user_name"))
                .login(resultSet.getString("login"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
        user.setFriendships(getFriendships(user.getId()));
        return user;
    }

    public Friendship mapRowToFriendship(ResultSet resultSet, int rowNum) throws SQLException {
        return Friendship.builder()
                .friendId(resultSet.getLong("friendId"))
                .approval(resultSet.getBoolean("approval"))
                .build();
    }

    public Set<Friendship> getFriendships(long userId) {
        return new HashSet<>(jdbcTemplate.query(GET_FRIENDS_OF_ONE_USER_FROM_DB, this::mapRowToFriendship, userId));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(jdbcTemplate.query(GET_ONE_USER_FROM_DB, this::mapRowToFilm));
    }

    @Override
    public User getUser(long id) {
        return jdbcTemplate.queryForObject(GET_ONE_USER_FROM_DB, this::mapRowToFilm, id);
    }

    @Override
    public boolean containsUser(long id) {
        SqlRowSet userRowSet = jdbcTemplate.queryForRowSet(GET_ONE_USER_FROM_DB, id);
        return userRowSet.next();
    }
}
