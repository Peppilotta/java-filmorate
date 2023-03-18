package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmDoesNotExistException;
import ru.yandex.practicum.filmorate.exception.GenreDoesNotExistException;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String GET_FILMS = "select * from Films ORDER BY film_id ";
    private static final String GET_BEST_FILMS =
            "select f.film_id, f.film_name, f.release_date, f.description, " +
                    "f.duration, f.rate, count(l.user_id) as likes" +
                    " from Films f left join likes l on f.film_id = l.film_id" +
                    "  group by f.film_id ORDER BY likes desc limit ?";
    private static final String GET_FILM = "select * from Films where film_id=?";
    private static final String GET_FILM_ID = "select film_id from Films where film_id=?";
    private static final String GET_FILM_LIKES = "select * from Likes where film_id=? ORDER BY user_id ";
    private static final String GET_FILM_GENRES =
            "select f.genre_id, g.genre_name from film_genres f " +
                    "right join genre g on f.genre_id = g.genre_id where film_id=? ";
    private static final String GET_FILM_MPAS =
            "select f.mpa_id, m.mpa_name from film_mpas f right join mpa m on f.mpa_id = m.mpa_id where f.film_id=? ";
    private static final String GET_GENRES = "select * from Genre ORDER BY genre_id ";
    private static final String GET_GENRE = "select * from Genre where genre_id=?";
    private static final String GET_MPAS = "select * from MPA ORDER BY mpa_id ";
    private static final String GET_MPA = "select * from MPA where mpa_id=?";

    private static final String UPDATE_FILM = "update Films set film_name=?, release_date=?, " +
            "description=?, duration=?, rate=? where film_id=? ";
    private static final String DELETE_LIKE = "DELETE FROM likes where film_id=? and user_id=? ";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genres where film_id=? and genre_id=? ";
    private static final String DELETE_FILM_MPA = "DELETE FROM film_mpas where film_id=? and mpa_id=? ";
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film getFilm(long id) {
        containsFilm(id);
        Film filmOut = getFilmOnly(id).get();
        getFilmGenres(id).ifPresent(filmOut::setGenres);
        getFilmMpa(id).ifPresent(filmOut::setMpa);
        return filmOut;
    }

    @Override
    public List<Film> getFilms() {
        return getSomeFilms(getFilmsOnly());
    }

    @Override
    public Genre getGenre(long id) {
        return getGenreOnly(id).get();
    }

    @Override
    public Set<Genre> getGenres() {
        return getGenresOnly().get();
    }

    @Override
    public Mpa getMpa(long id) {
        return getMpaOnly(id).get();
    }

    @Override
    public Set<Mpa> getMpas() {
        return getMpasOnly().get();
    }

    @Override
    public Film create(Film film) {
        film.setId(saveFilm(film));
        Mpa mpa = film.getMpa();
        film.setMpa(getMpa(mpa.getId()));
        updateFilmMpa(film.getMpa(), film.getId());
        Set<Genre> genres = film.getGenres();
        if (!genres.isEmpty()) {
            updateFilmGenres(removeDoubles(genres), film.getId());
        }
        return film;
    }

    @Override
    public boolean addLike(long filmId, long userId) {
        Optional<Film> film = getFilmOnly(filmId);
        if (film.isEmpty()) {
            throw new FilmDoesNotExistException("Film with id=" + film + " not exist. ");
        }
        long likeCreated = saveLike(new Like(filmId, userId));
        return likeCreated > 0;
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        Optional<Film> film = getFilmOnly(filmId);
        if (film.isEmpty()) {
            throw new FilmDoesNotExistException("Film with id=" + film + " not exist. ");
        }
        long likeDeleted = jdbcTemplate.update(DELETE_LIKE, filmId, userId);
        Optional<Set<Long>> likes = getFilmLikes(filmId);
        if (likes.isPresent()) {
            likes.get().size();
        }
        return likeDeleted > 0;
    }

    @Override
    public Film update(Film film) {
        long filmId = film.getId();
        containsFilm(filmId);
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getReleaseDate(),
                film.getDescription(), film.getDuration(), film.getRate(), filmId);
        Mpa mpaBefore = getFilmMpa(filmId).get();
        Mpa mpaAfter = film.getMpa();
        film.setMpa(getMpa(mpaAfter.getId()));
        if (!Objects.equals(mpaAfter, mpaBefore)) {
            deleteFilmMpa(mpaBefore, filmId);
            updateFilmMpa(mpaAfter, filmId);
        }
        Set<Genre> genresBefore = getFilmGenres(filmId).get();
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

    private long saveMpa(Mpa mpa) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("mpa")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(mpa.toMap()).longValue();
    }

    private long saveGenre(Genre genre) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("genre")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(genre.toMap()).longValue();
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

    private Genre mapRowToGenre(ResultSet rs, long rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("genre_id"))
                .name(rs.getString("genre_name"))
                .build();
    }

    private Mpa mapRowToMpa(ResultSet rs, long rowNum) throws SQLException {
        return Mpa.builder()
                .id(rs.getLong("mpa_id"))
                .name(rs.getString("mpa_name"))
                .build();
    }

    public List<Film> getFavoriteFilms(int count) {
        return getSomeFilms(getBestFilmsOnly(count));
    }

    public Optional<Set<Genre>> getFilmGenres(long filmId) {
        return Optional.of(new LinkedHashSet<>(jdbcTemplate.query(GET_FILM_GENRES, this::mapRowToFilmGenre, filmId)));
    }

    public Optional<Mpa> getFilmMpa(long filmId) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(GET_FILM_MPAS, this::mapRowToFilmMpa, filmId));
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA for ffilm id=" + filmId + " not exist. ");
        }
    }

    private Optional<Set<Long>> getFilmLikes(long filmId) {
        return Optional.of(new LinkedHashSet<>(jdbcTemplate.query(GET_FILM_LIKES, this::mapRowToLike, filmId)));
    }

    private Optional<Film> getFilmOnly(long filmId) {
        return Optional.of(jdbcTemplate.queryForObject(GET_FILM, this::mapRowToFilm, filmId));
    }

    private boolean containsMpa(long id) {
        try {
            jdbcTemplate.queryForObject(GET_MPA, this::mapRowToMpa, id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA with id=" + id + " not exist. ");
        }
    }

    private boolean containsGenre(long id) {
        try {
            jdbcTemplate.queryForObject(GET_GENRE, this::mapRowToGenre, id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            throw new GenreDoesNotExistException("Genre with id=" + id + " not exist. ");
        }
    }

    private Optional<List<Film>> getFilmsOnly() {
        try {
            return Optional.of(new LinkedList<>(jdbcTemplate.query(GET_FILMS, this::mapRowToFilm)));
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Empty list");
        }
    }

    private Optional<List<Film>> getBestFilmsOnly(int count) {
        try {
            return Optional.of(new LinkedList<>(jdbcTemplate.query(GET_BEST_FILMS, this::mapRowToFilm, count)));
        } catch (EmptyResultDataAccessException e) {
            throw new FilmDoesNotExistException("Empty list");
        }
    }

    public Optional<Genre> getGenreOnly(long id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(GET_GENRE, this::mapRowToGenre, id));
        } catch (EmptyResultDataAccessException e) {
            throw new GenreDoesNotExistException("Genre with id=" + id + " not exist. ");
        }
    }

    private List<Film> getSomeFilms(Optional<List<Film>> films) {
        if (films.isEmpty()) {
            return new ArrayList<>();
        }
        List<Film> filmsOut = films.get();
        filmsOut.forEach(f -> {
            long id = f.getId();
            Optional<Set<Genre>> genres = getFilmGenres(id);
            genres.ifPresent(f::setGenres);
            Optional<Mpa> mpa = getFilmMpa(id);
            mpa.ifPresent(f::setMpa);
        });
        return filmsOut;
    }

    private Set<Genre> removeDoubles(Set<Genre> genres) {
        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        return genreIds.stream().map(this::getGenre).collect(Collectors.toSet());
    }

    private Optional<Mpa> getMpaOnly(long id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(GET_MPA, this::mapRowToMpa, id));
        } catch (EmptyResultDataAccessException e) {
            throw new MpaDoesNotExistException("MPA with id=" + id + " not exist. ");
        }
    }

    private Optional<Set<Genre>> getGenresOnly() {
        return Optional.of(new LinkedHashSet<>(jdbcTemplate.query(GET_GENRES, this::mapRowToGenre)));
    }

    private Optional<Set<Mpa>> getMpasOnly() {
        return Optional.of(new LinkedHashSet<>(jdbcTemplate.query(GET_MPAS, this::mapRowToMpa)));
    }

    private boolean updateFilmMpa(Mpa mpa, long filmId) {
        containsMpa(mpa.getId());
        return saveFilmMpa(FilmMpa.builder()
                .filmId(filmId)
                .mpaId(mpa.getId())
                .build()) > 0;
    }

    private boolean updateFilmGenres(Set<Genre> genres, long filmId) {
        long count = 0;
        for (Genre genre : genres) {
            containsGenre(genre.getId());
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