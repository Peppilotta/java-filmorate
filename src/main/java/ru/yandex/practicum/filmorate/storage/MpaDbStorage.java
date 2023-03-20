package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class MpaDbStorage implements MpaStorage {

    private static final String GET_MPAS = "SELECT * FROM MPA ORDER BY mpa_id ";

    private static final String GET_MPA = "SELECT * from MPA WHERE mpa_id=?";

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpa(long id) {
        try {
            return jdbcTemplate.queryForObject(GET_MPA, this::mapRowToMpa, id);
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA with id=" + id + " not exist. ");
        }
    }

    @Override
    public Set<Mpa> getMpas() {
        try {
            return new LinkedHashSet<>(jdbcTemplate.query(GET_MPAS, this::mapRowToMpa));
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA list not exist. ");
        }
    }

    @Override
    public long saveMpa(Mpa mpa) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("mpa")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(mpa.toMap()).longValue();
    }

    @Override
    public boolean containsMpa(long id) {
        try {
            jdbcTemplate.queryForObject(GET_MPA, this::mapRowToMpa, id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA with id=" + id + " not exist. ");
        }
    }

    private Mpa mapRowToMpa(ResultSet rs, long rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getLong("mpa_id"))
                .name(rs.getString("mpa_name"))
                .build();
    }
}
