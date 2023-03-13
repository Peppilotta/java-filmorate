package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String GET_FILMS_FROM_DB = "select * from Films";
    private static final String GET_ONE_FILM_FROM_DB = "select * from Films where film_id=?";
    private static final String PUT_ONE_FILM_TO_DB = "insert into Films (film_name," +
            "description, " +
            "release_date, " +
            "duration, " +
            "genre_id, " +
            "rating_id) values (?,?,?,?,?,?)";

    private static final String UPDATE_ONE_FILM_TO_DB = "update Films set film_name=?, " +
            "description=?, " +
            "release_date=?, " +
            "duration=?, " +
            "genre_id=?, " +
            "rating_id=? where film_id=? ";
    private static final String GET_LIKES_OF_ONE_FILM_FROM_DB = "select * from Likes where film_id=?";
    private static final String PUT_LIKE_OF_ONE_FILM_TO_DB = "insert into Likes (film_id, user_id) values (?, ?)";
    private static final String DELETE_LIKES_OF_ONE_FILM_FROM_DB = "delete from Likes where film_id=?";
    private static final String GET_FILM_MAX_ID = "SELECT MAX(film_id) as max_id from Films";

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        jdbcTemplate.update(PUT_ONE_FILM_TO_DB,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getGenreId(),
                film.getRatingId());
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(GET_FILM_MAX_ID);
        long filmId = sqlRowSet.getLong("max_id");
        Set<Long> likes = film.getLikeIds();
        if (!likes.isEmpty()) {
            addLikesToDb(filmId, likes);
        }
        return getFilm(filmId);
    }

    public void addLikeToDb(long filmId, long userId) {
        jdbcTemplate.update(PUT_LIKE_OF_ONE_FILM_TO_DB, filmId, userId);
    }

    public void addLikesToDb(long filmId, Set<Long> likes) {
        for (Long userId : likes) {
            addLikeToDb(filmId, userId);
        }
    }

    @Override
    public Film update(Film film) {
        long filmId = film.getId();
        if (containsFilm(filmId)) {
            jdbcTemplate.update(UPDATE_ONE_FILM_TO_DB,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getGenreId(),
                    film.getRatingId(),
                    filmId);
            int keyDelete = jdbcTemplate.update(DELETE_LIKES_OF_ONE_FILM_FROM_DB, filmId);
            addLikesToDb(filmId, film.getLikeIds());
            return getFilm(filmId);
        }
        return null;
    }

    @Override
    public List<Film> getFilms() {
        return jdbcTemplate.query(GET_FILMS_FROM_DB, this::mapRowToFilm);
    }

    @Override
    public Film getFilm(long id) {
        return jdbcTemplate.queryForObject(GET_ONE_FILM_FROM_DB, this::mapRowToFilm, id);
    }

    @Override
    public boolean containsFilm(long id) {
        SqlRowSet filmRowSet = jdbcTemplate.queryForRowSet(GET_FILMS_FROM_DB, id);
        return filmRowSet.next();
    }

    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("film_name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .genreId(resultSet.getInt("genre_id"))
                .ratingId(resultSet.getInt("rating_id"))
                .build();
        film.setLikeIds(getLikes(film.getId()));
        return film;
    }

    private long mapRowToLike(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getInt("user_id");
    }

    private Set<Long> getLikes(long filmId) {
        return new HashSet<>(jdbcTemplate.query(GET_LIKES_OF_ONE_FILM_FROM_DB, this::mapRowToLike, filmId));
    }
}
