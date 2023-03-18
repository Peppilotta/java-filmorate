package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String GET_USERS = "select * from Users order by user_id ";
    private static final String GET_USER = "select * from Users where user_id=?";
    private static final String GET_USER_ID = "select user_id from Users where user_id=?";
    private static final String UPDATE_USER =
            "update Users set user_name=?, login=?, email=?, birthday=? where user_id=? ";
    private static final String GET_USER_FRIENDS = "select * from Friends where user_id=? order by friend_id ";
    private static final String GET_USER_FRIENDS_AS_USERS =
            "select * from users where user_id in (select friend_id from friends where user_id=? ) order by user_id";
    private static final String GET_COMMON_FRIENDS_AS_USERS =
            "select * from users where user_id in (select friend_id from friends where user_id=? and friend_id in " +
                    "(select friend_id from friends where user_id=?)) order by user_id";
    private static final String DELETE_FRIENDSHIP = "delete from friends where user_id=? and friend_id=?";

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    private long mapRowToUserId(ResultSet rs, long rowNum) throws SQLException {
        return rs.getLong("user_id");
    }

    public Friendship mapRowToFriendship(ResultSet rs, long rowNum) throws SQLException {
        return Friendship.builder()
                .userId(rs.getLong("user_id"))
                .friendId(rs.getLong("friend_id"))
                .approval(rs.getBoolean("approval"))
                .build();
    }

    private Optional<Set<Friendship>> getFriendships(long userId) {
        return Optional.of(new HashSet<>
                (jdbcTemplate.query(GET_USER_FRIENDS, this::mapRowToFriendship, userId)));
    }

    private Optional<List<User>> getUsersOnly() {
        return Optional.of(new ArrayList<>(jdbcTemplate.query(GET_USERS, this::mapRowToUser)));
    }

    private Optional<List<User>> getFriendsAsUser(long userId) {
        return Optional.of(new ArrayList<>
                (jdbcTemplate.query(GET_USER_FRIENDS_AS_USERS, this::mapRowToUser, userId)));
    }

    private Optional<List<User>> getCommonFriendsAsUser(long userId, long otherUserId) {
        return Optional.of(new ArrayList<>
                (jdbcTemplate.query(GET_COMMON_FRIENDS_AS_USERS, this::mapRowToUser, userId, otherUserId)));
    }

    private Optional<User> getUserOnly(long userId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(GET_USER, this::mapRowToUser, userId));
        } catch (EmptyResultDataAccessException e) {
            throw new UserDoesNotExistException("User with id=" + userId + " not exist. ");
        }
    }

    @Override
    public boolean containsUser(long userId) {
        try {
            jdbcTemplate.queryForObject(GET_USER_ID, this::mapRowToUserId, userId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            throw new UserDoesNotExistException("User with id=" + userId + " not exist. ");
        }
    }

    private boolean updateFriends(Set<Friendship> friendships) {
        long count = 0;
        for (Friendship friendship : friendships) {
            count += saveFriendship(Friendship.builder()
                    .userId(friendship.getUserId())
                    .friendId(friendship.getFriendId())
                    .approval(friendship.isApproval())
                    .build());
        }
        return count == friendships.size();
    }

    private boolean deleteFriends(Set<Friendship> friendships) {
        long count = 0;
        for (Friendship friendship : friendships) {
            count += jdbcTemplate.update(DELETE_FRIENDSHIP, friendship.getUserId(), friendship.getFriendId());
        }
        return count == friendships.size();
    }

    @Override
    public User create(User user) {
        long userId = saveUser(user);
        Set<Friendship> friends = user.getFriendships();
        if (!friends.isEmpty()) {
            updateFriends(friends);
        }
        return getUser(userId);
    }

    @Override
    public User update(User user) {
        long userId = user.getId();
        containsUser(userId);
        jdbcTemplate.update(UPDATE_USER, user.getName(), user.getLogin(),
                user.getEmail(), user.getBirthday(), userId);
        Set<Friendship> friendsBefore = getFriendships(userId).get();
        Set<Friendship> friendsAfter = user.getFriendships();
        int beforeNum = 0;
        int afterNum = 0;
        if (!friendsBefore.isEmpty()) {
            beforeNum += 1;
        }
        if (!friendsAfter.isEmpty()) {
            afterNum += 2;
        }
        switch (beforeNum + afterNum) {
            case 1:
                deleteFriends(friendsBefore);
                break;
            case 2:
                updateFriends(friendsAfter);
                break;
            case 3:
                Set<Friendship> friendsForDelete = friendsBefore
                        .stream()
                        .filter(f -> !friendsAfter.contains(f))
                        .collect(Collectors.toSet());
                Set<Friendship> friendsForUpdate = friendsAfter
                        .stream()
                        .filter(f -> !friendsBefore.contains(f))
                        .collect(Collectors.toSet());
                if (!friendsForDelete.isEmpty()) {
                    deleteFriends(friendsForDelete);
                }
                if (!friendsForUpdate.isEmpty()) {
                    updateFriends(friendsForUpdate);
                }
                break;
            default:
                break;
        }
        return getUser(userId);
    }

    @Override
    public List<User> getUsers() {
        return getSomeUsers(getUsersOnly());
    }

    @Override
    public User getUser(long id) {
        Optional<User> user = getUserOnly(id);
        User userOut = user.get();
        Optional<Set<Friendship>> friends = getFriendships(id);
        friends.ifPresent(userOut::setFriendships);
        return userOut;
    }

    @Override
    public boolean addFriend(long userId, long friendId) {
        containsUser(userId);
        containsUser(friendId);
        long friendCreated = saveFriendship(new Friendship(userId, friendId, false));
        update(getUser(userId));
        long otherFriendCreated = saveFriendship(new Friendship(friendId, userId, false));
        update(getUser(friendId));
        return friendCreated > 0 && otherFriendCreated > 0;
    }

    @Override
    public boolean deleteFriend(long userId, long friendId) {
        containsUser(userId);
        containsUser(friendId);
        long friendDeleted = jdbcTemplate.update(DELETE_FRIENDSHIP, friendId, userId);
        update(getUser(userId));
        return friendDeleted > 0;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherUserId) {
        containsUser(userId);
        containsUser(otherUserId);
        return getSomeUsers(getCommonFriendsAsUser(userId, otherUserId));
    }

    public List<User> getSomeUsers(Optional<List<User>> users) {
        if (users.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> usersOut = users.get();
        usersOut.forEach(f -> {
            long id = f.getId();
            Optional<Set<Friendship>> friends = getFriendships(id);
            friends.ifPresent(f::setFriendships);
        });
        return usersOut;
    }

    @Override
    public List<User> getFriends(long userId) {
        containsUser(userId);
        return getSomeUsers(getFriendsAsUser(userId));
    }
}