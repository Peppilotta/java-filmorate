package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ItemDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class GenreDbStorage implements GenreStorage {
    private static final String GET_GENRES = "SELECT * FROM genre ORDER BY genre_id ";

    private static final String GET_GENRE = "SELECT * FROM genre WHERE genre_id=?";

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenre(long id) {
        try {
            return jdbcTemplate.queryForObject(GET_GENRE, this::mapRowToGenre, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ItemDoesNotExistException("Genre with id=" + id + " not exist. ");
        }
    }

    @Override
    public Set<Genre> getGenres() {
        try {
            return new LinkedHashSet<>(jdbcTemplate.query(GET_GENRES, this::mapRowToGenre));
        } catch (EmptyResultDataAccessException e) {
            throw new ItemDoesNotExistException("Genre list not exist. ");
        }
    }

    @Override
    public boolean containsGenre(long id) {
        return jdbcTemplate.queryForRowSet(GET_GENRE, id).next();
    }

    @Override
    public long saveGenre(Genre genre) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("genre")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(genre.toMap()).longValue();
    }

    private Genre mapRowToGenre(ResultSet rs, long rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("genre_id"))
                .name(rs.getString("genre_name"))
                .build();
    }
}