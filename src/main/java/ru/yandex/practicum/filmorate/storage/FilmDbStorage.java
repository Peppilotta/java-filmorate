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
import ru.yandex.practicum.filmorate.model.GenreId;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.MpaId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
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
    private static final String GET_BEST_FILMS = "select * from Films ORDER BY rate desc limit ?";
    private static final String GET_FILM = "select * from Films where film_id=?";
    private static final String GET_FILM_ID = "select film_id from Films where film_id=?";
    private static final String GET_FILM_LIKES = "select * from Likes where film_id=? ORDER BY user_id ";
    private static final String GET_FILM_GENRES = "select * from film_genres where film_id=? ";
    private static final String GET_FILM_MPAS = "select * from film_mpas where film_id=? ORDER BY mpa_id ";
    private static final String GET_GENRES = "select * from Genre ORDER BY genre_id ";
    private static final String GET_GENRE = "select * from Genre where genre_id=?";
    private static final String GET_MPAS = "select * from MPA ORDER BY mpa_id ";
    private static final String GET_MPA = "select * from MPA where mpa_id=?";

    private static final String UPDATE_FILM = "update Films set film_name=?, release_date=?, " +
            "description=?, duration=?, rate=? where film_id=? ";
    private static final String UPDATE_FILM_RATE = "update Films set rate=? where film_id=? ";
    private static final String DELETE_LIKE = "DELETE FROM likes where film_id=? and user_id=? ";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genres where film_id=? and genre_id=? ";
    private static final String DELETE_FILM_MPA = "DELETE FROM film_mpas where film_id=? and mpa_id=? ";
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    private GenreId mapRowToFilmGenre(ResultSet rs, long rowNum) throws SQLException {
        return GenreId.builder()
                .id(rs.getInt("genre_id"))
                .build();
    }

    private MpaId mapRowToFilmMpa(ResultSet rs, long rowNum) throws SQLException {
        return MpaId.builder()
                .id(rs.getInt("mpa_id"))
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

    public Optional<Set<GenreId>> getFilmGenres(long filmId) {
        return Optional.of(new LinkedHashSet<>(jdbcTemplate.query(GET_FILM_GENRES, this::mapRowToFilmGenre, filmId)));
    }

    public Optional<MpaId> getFilmMpa(long filmId) {
        return Optional.of(jdbcTemplate.queryForObject(GET_FILM_MPAS, this::mapRowToFilmMpa, filmId));
    }

    private Optional<Set<Long>> getFilmLikes(long filmId) {
        return Optional.of(new LinkedHashSet<>(jdbcTemplate.query(GET_FILM_LIKES, this::mapRowToLike, filmId)));
    }

    private Optional<Film> getFilmOnly(long filmId) {
        return Optional.of(jdbcTemplate.queryForObject(GET_FILM, this::mapRowToFilm, filmId));
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

    private boolean updateFilmMpa(MpaId mpa, long filmId) {
        return saveFilmMpa(FilmMpa
                .builder()
                .filmId(filmId)
                .mpaId(mpa.getId())
                .build()) > 0;
    }

    private boolean updateFilmGenres(Set<GenreId> genres, long filmId) {
        long count = 0;
        for (GenreId genre : genres) {
            count += saveFilmGenre(FilmGenre
                    .builder()
                    .filmId(filmId)
                    .genreId(genre.getId())
                    .build());
        }
        return count == genres.size();
    }

    private boolean deleteFilmMpa(MpaId mpa, long filmId) {
        return jdbcTemplate.update(DELETE_FILM_MPA, filmId, mpa.getId()) > 0;
    }

    private boolean deleteFilmGenres(Set<GenreId> genres, long filmId) {
        long count = 0;
        for (GenreId genre : genres) {
            count += jdbcTemplate.update(DELETE_FILM_GENRE, filmId, genre.getId());
        }
        return count == genres.size();
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

    public List<Film> getSomeFilms(Optional<List<Film>> films) {
        List<Film> filmsOut = films.get();
        filmsOut.forEach(f -> {
            long id = f.getId();
            Optional<Set<GenreId>> genres = getFilmGenres(id);
            genres.ifPresent(f::setGenres);
            Optional<MpaId> mpa = getFilmMpa(id);
            mpa.ifPresent(f::setMpa);
        });
        return filmsOut;
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
        Optional<Set<Mpa>> mpas = getMpasOnly();
        if (mpas.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return mpas.get();
    }

    @Override
    public Film create(Film film) {
        film.setId(saveFilm(film));
        updateFilmMpa(film.getMpa(), film.getId());
        updateFilmGenres(film.getGenres(), film.getId());
        return film;
    }

    @Override
    public boolean addLike(long filmId, long userId) {
        Optional<Film> film = getFilmOnly(filmId);
        if (film.isEmpty()) {
            throw new FilmDoesNotExistException("Film with id=" + film + " not exist. ");
        }
        long likeCreated = saveLike(new Like(filmId, userId));
        Optional<Set<Long>> likes = getFilmLikes(filmId);
        long count = 1;
        if (likes.isPresent()) {
            count += likes.get().size();
        }
        long filmUpdated = jdbcTemplate.update(UPDATE_FILM_RATE, count, filmId);
        return filmUpdated > 0 && likeCreated > 0;
    }

    @Override
    public boolean deleteLike(long filmId, long userId) {
        Optional<Film> film = getFilmOnly(filmId);
        if (film.isEmpty()) {
            throw new FilmDoesNotExistException("Film with id=" + film + " not exist. ");
        }
        long likeDeleted = jdbcTemplate.update(DELETE_LIKE, filmId, userId);
        long countLikes = 0;
        Optional<Set<Long>> likes = getFilmLikes(filmId);
        if (likes.isPresent()) {
            countLikes += likes.get().size();
        }
        long filmUpdated = jdbcTemplate.update(UPDATE_FILM_RATE, countLikes, filmId);
        return filmUpdated > 0 && likeDeleted > 0;
    }

    @Override
    public Film update(Film film) {
        long filmId = film.getId();
        containsFilm(filmId);
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getReleaseDate(),
                film.getDescription(), film.getDuration(), film.getRate(), filmId);
        MpaId mpaIdBefore = getFilmMpa(filmId).get();
        MpaId mpaIdAfter = film.getMpa();
        if (!Objects.equals(mpaIdAfter, mpaIdBefore)) {
            deleteFilmMpa(mpaIdBefore, filmId);
            updateFilmMpa(mpaIdAfter, filmId);
        }
        Set<GenreId> genresBefore = getFilmGenres(filmId).get();
        Set<GenreId> genresAfter = film.getGenres();
        int beforeNum = 0;
        int afterNum = 0;
        if (!genresBefore.isEmpty()) {
            beforeNum += 1;
        }
        if (!genresAfter.isEmpty()) {
            afterNum += 2;
        }

        switch (beforeNum + afterNum) {
            case 1:
                deleteFilmGenres(genresBefore, filmId);
                break;
            case 2:
                updateFilmGenres(genresAfter, filmId);
                break;
            case 3:
                Set<GenreId> genresForDelete = genresBefore
                        .stream()
                        .filter(g -> !genresAfter.contains(g))
                        .collect(Collectors.toSet());
                Set<GenreId> genresForUpdate = genresAfter
                        .stream()
                        .filter(g -> !genresBefore.contains(g))
                        .collect(Collectors.toSet());
                if (!genresForDelete.isEmpty()) {
                    deleteFilmGenres(genresForDelete, filmId);
                }
                if (!genresForUpdate.isEmpty()) {
                    updateFilmGenres(genresForUpdate, filmId);
                }
        }
        return getFilm(filmId);
    }

    public List<Film> getFavoriteFilms(int count) {
        return getSomeFilms(getBestFilmsOnly(count));
    }
}