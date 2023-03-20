package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.MpaDoesNotExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String GET_FILMS = "SELECT * FROM Films ORDER BY film_id ";

    private static final String GET_BEST_FILMS =
            "SELECT f.film_id, f.film_name, f.release_date, f.description, " +
                    "f.duration, f.rate, count(l.user_id) AS likes" +
                    " FROM Films f LEFT JOIN likes l ON f.film_id = l.film_id" +
                    "  GROUP BY f.film_id ORDER BY likes DESC LIMIT ?";

    private static final String GET_FILM = "SELECT * FROM Films WHERE film_id=?";

    private static final String GET_FILM_ID = "SELECT film_id FROM Films WHERE film_id=?";

    private static final String GET_FILM_LIKES = "SELECT * FROM Likes WHERE film_id=? ORDER BY user_id ";

    private static final String GET_FILM_GENRES =
            "SELECT f.genre_id, g.genre_name FROM film_genres f " +
                    "RIGHT JOIN genre g ON f.genre_id = g.genre_id WHERE film_id=? ";

    private static final String GET_FILM_MPAS =
            "SELECT f.mpa_id, m.mpa_name FROM film_mpas f right JOIN mpa m ON f.mpa_id = m.mpa_id WHERE f.film_id=? ";

    private static final String UPDATE_FILM = "UPDATE Films set film_name=?, release_date=?, " +
            "description=?, duration=?, rate=? WHERE film_id=? ";

    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id=? AND user_id=? ";

    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genres WHERE film_id=? AND genre_id=? ";

    private static final String DELETE_FILM_MPA = "DELETE FROM film_mpas WHERE film_id=? AND mpa_id=? ";

    private final JdbcTemplate jdbcTemplate;

    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Film getFilm(long id) {
        Film filmOut = getFilmOnly(id);
        filmOut.setGenres(getFilmGenres(id));
        filmOut.setMpa(getFilmMpa(id));
        return filmOut;
    }

    @Override
    public List<Film> getFilms() {
        return getSomeFilms(getFilmsOnly());
    }

    @Override
    public Film create(Film film) {
        film.setId(saveFilm(film));
        Mpa mpa = film.getMpa();
        film.setMpa(mpaStorage.getMpa(mpa.getId()));
        updateFilmMpa(film.getMpa(), film.getId());
        Set<Genre> genres = film.getGenres();
        if (!genres.isEmpty()) {
            updateFilmGenres(removeDoubles(genres), film.getId());
        }
        return film;
    }

    @Override
    public boolean addLike(long filmId, long userId) {
        long likeCreated = saveLike(new Like(filmId, userId));
        return likeCreated > 0;
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        long likeDeleted = jdbcTemplate.update(DELETE_LIKE, filmId, userId);
        return likeDeleted > 0;
    }

    @Override
    public List<Film> getTheMostPopularFilms(int count) {
        return getSomeFilms(getFavoriteFilms(count));
    }

    @Override
    public Film update(Film film) {
        long filmId = film.getId();
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getReleaseDate(),
                film.getDescription(), film.getDuration(), film.getRate(), filmId);
        Mpa mpaBefore = getFilmMpa(filmId);
        Mpa mpaAfter = film.getMpa();
        film.setMpa(mpaStorage.getMpa(mpaAfter.getId()));
        if (!Objects.equals(mpaAfter, mpaBefore)) {
            deleteFilmMpa(mpaBefore, filmId);
            updateFilmMpa(mpaAfter, filmId);
        }
        Set<Genre> genresBefore = getFilmGenres(filmId);
        Set<Genre> genresAfter = removeDoubles(film.getGenres());
        if (!genresBefore.isEmpty()) {
            deleteFilmGenres(genresBefore, filmId);
        }
        if (!genresAfter.isEmpty()) {
            updateFilmGenres(genresAfter, filmId);
        }
        return getFilm(filmId);
    }

    @Override
    public boolean containsFilm(long filmId) {
        try {
            jdbcTemplate.queryForObject(GET_FILM_ID, this::mapRowToFilmId, filmId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Film with id=" + filmId + " not exist. ");
        }
    }

    private long saveFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        return simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();
    }

    private long saveLike(Like like) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("likes")
                .usingGeneratedKeyColumns("like_id");
        return simpleJdbcInsert.executeAndReturnKey(like.toMap()).longValue();
    }

    private long saveFilmMpa(FilmMpa mpa) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film_mpas")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(mpa.toMap()).longValue();
    }

    private long saveFilmGenre(FilmGenre genre) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film_genres")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(genre.toMap()).longValue();
    }

    private Film mapRowToFilm(ResultSet rs, long rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("film_name"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .description(rs.getString("description"))
                .duration(rs.getLong("duration"))
                .rate(rs.getInt("rate"))
                .build();
    }

    private long mapRowToFilmId(ResultSet rs, long rowNum) throws SQLException {
        return rs.getLong("film_id");
    }

    private Genre mapRowToFilmGenre(ResultSet rs, long rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("genre_id"))
                .name(rs.getString("genre_name"))
                .build();
    }

    private Mpa mapRowToFilmMpa(ResultSet rs, long rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getLong("mpa_id"))
                .name(rs.getString("mpa_name"))
                .build();
    }

    private long mapRowToLike(ResultSet rs, long rowNum) throws SQLException {
        return rs.getLong("user_id");
    }

    public List<Film> getFavoriteFilms(int count) {
        try {
            return new LinkedList<>(jdbcTemplate.query(GET_BEST_FILMS, this::mapRowToFilm, count));
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Empty list");
        }
    }

    public Set<Genre> getFilmGenres(long filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query(GET_FILM_GENRES, this::mapRowToFilmGenre, filmId));
    }

    public Mpa getFilmMpa(long filmId) {
        try {
            return jdbcTemplate.queryForObject(GET_FILM_MPAS, this::mapRowToFilmMpa, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA for film id=" + filmId + " not exist. ");
        }
    }

    private Set<Long> getFilmLikes(long filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query(GET_FILM_LIKES, this::mapRowToLike, filmId));
    }

    private Film getFilmOnly(long filmId) {
        try {
            return jdbcTemplate.queryForObject(GET_FILM, this::mapRowToFilm, filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Film with id=" + filmId + " not exist. ");
        }
    }

    private List<Film> getFilmsOnly() {
        try {
            return new LinkedList<>(jdbcTemplate.query(GET_FILMS, this::mapRowToFilm));
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Empty list");
        }
    }

    private List<Film> getBestFilmsOnly(int count) {
        try {
            return new LinkedList<>(jdbcTemplate.query(GET_BEST_FILMS, this::mapRowToFilm, count));
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Empty list");
        }
    }

    private List<Film> getSomeFilms(List<Film> films) {
        if (films.isEmpty()) {
            return new ArrayList<>();
        }
        films.forEach(f -> {
            long id = f.getId();
            f.setGenres(getFilmGenres(id));
            f.setMpa(getFilmMpa(id));
        });
        return films;
    }

    private Set<Genre> removeDoubles(Set<Genre> genres) {
        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        return genreIds.stream().map(g -> genreStorage.getGenre(g)).collect(Collectors.toSet());
    }

    private boolean updateFilmMpa(Mpa mpa, long filmId) {
        mpaStorage.containsMpa(mpa.getId());
        return saveFilmMpa(FilmMpa.builder()
                .filmId(filmId)
                .mpaId(mpa.getId())
                .build()) > 0;
    }

    private boolean updateFilmGenres(Set<Genre> genres, long filmId) {
        long count = 0;
        for (Genre genre : genres) {
            genreStorage.containsGenre(genre.getId());
            count += saveFilmGenre(FilmGenre
                    .builder()
                    .filmId(filmId)
                    .genreId(genre.getId())
                    .build());
        }
        return count == genres.size();
    }

    private boolean deleteFilmMpa(Mpa mpa, long filmId) {
        return jdbcTemplate.update(DELETE_FILM_MPA, filmId, mpa.getId()) > 0;
    }

    private boolean deleteFilmGenres(Set<Genre> genres, long filmId) {
        long count = 0;
        for (Genre genre : genres) {
            count += jdbcTemplate.update(DELETE_FILM_GENRE, filmId, genre.getId());
        }
        return count == genres.size();
    }
}