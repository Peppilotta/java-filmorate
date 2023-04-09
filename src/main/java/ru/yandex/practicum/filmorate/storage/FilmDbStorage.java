package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String GET_FILMS =
            "SELECT F.FILM_ID AS ID, F.FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE, M.MPA_ID, " +
                    "MP.MPA_NAME FROM FILMS F LEFT JOIN FILM_MPAS M ON F.FILM_ID = M.FILM_ID " +
                    "LEFT JOIN MPA MP ON M.MPA_ID = MP.MPA_ID " +
                    "ORDER BY F.FILM_ID ";

    private static final String GET_BEST_FILMS =
            "SELECT F.FILM_ID AS ID, F.FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE, " +
                    "COUNT(L.USER_ID) AS liked, M.MPA_ID, MP.MPA_NAME " +
                    "FROM FILMS F " +
                    "LEFT JOIN FILM_MPAS M ON F.FILM_ID = M.FILM_ID " +
                    "LEFT JOIN MPA MP ON M.MPA_ID = MP.MPA_ID " +
                    "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID  " +
                    "GROUP BY F.FILM_ID " +
                    "ORDER BY LIKED DESC LIMIT ?";

    private static final String GET_FILM =
            "SELECT F.FILM_ID  AS ID, F.FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.RATE, " +
                    "M.MPA_ID, MP.MPA_NAME  FROM FILMS F " +
                    "LEFT JOIN FILM_MPAS M ON F.FILM_ID = M.FILM_ID " +
                    "LEFT JOIN MPA MP ON M.MPA_ID = MP.MPA_ID " +
                    "WHERE F.FILM_ID=? ";

    private static final String GET_FILM_GENRES =
            "SELECT f.genre_id AS id, g.genre_name AS name " +
                    "FROM film_genres f " +
                    "LEFT JOIN  genre g ON f.genre_id = g.genre_id " +
                    "WHERE f.film_id = ? " +
                    "ORDER BY g.genre_id";

    private static final String GET_FILM_ID = "SELECT film_id FROM films WHERE film_id=?";

    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?,?)";

    private static final String INSERT_FILM_MPA = "INSERT INTO film_mpas (film_id, mpa_id) VALUES (?,?)";

    private static final String INSERT_LIKE = "INSERT INTO likes (film_id, user_id) VALUES (?,?)";

    private static final String UPDATE_FILM = "UPDATE films SET film_name=?, release_date=?, " +
            "description=?, duration=?, rate=? WHERE film_id=? ";

    private static final String DELETE_LIKE = "DELETE FROM likes WHERE film_id=? AND user_id=? ";

    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genres WHERE film_id=? AND genre_id=? ";

    private static final String DELETE_FILM_MPA = "DELETE FROM film_mpas WHERE film_id=? AND mpa_id=? ";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film getFilm(long id) {
        return getRestrictedListOfFilms(GET_FILM, id).get(id);
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(getRestrictedListOfFilms(GET_FILMS, 0).values());
    }

    @Override
    public Film create(Film film) {
        film.setId(saveFilm(film));
        long filmId = film.getId();
        Mpa mpa = film.getMpa();
        updateFilmMpa(mpa.getId(), filmId);
        Set<Genre> genres = film.getGenres();
        if (!genres.isEmpty()) {
            updateFilmGenres(removeDoubles(genres), filmId);
        }
        return getFilm(filmId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbcTemplate.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        jdbcTemplate.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public List<Film> getTheMostPopularFilms(int count) {
        return new LinkedList<>(getRestrictedListOfFilms(GET_BEST_FILMS, count).values());
    }

    @Override
    public Film update(Film film) {
        long filmId = film.getId();
        jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getReleaseDate(),
                film.getDescription(), film.getDuration(), film.getRate(), filmId);
        Film filmBefore = getFilm(filmId);
        Mpa mpaBefore = filmBefore.getMpa();
        Mpa mpaAfter = film.getMpa();
        if (!Objects.equals(mpaAfter.getId(), mpaBefore.getId())) {
            deleteFilmMpa(mpaBefore, filmId);
            updateFilmMpa(mpaAfter.getId(), filmId);
        }
        Set<Genre> genresBefore = filmBefore.getGenres();
        Set<Long> genresAfter = removeDoubles(film.getGenres());
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
        return jdbcTemplate.queryForRowSet(GET_FILM_ID, filmId).next();
    }

    private long saveFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        return simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();
    }

    private Map<Long, Film> getRestrictedListOfFilms(String sql, long id) {
        Map<Long, Film> films = new LinkedHashMap<>();
        SqlRowSet rs;
        if (id == 0) {
            rs = jdbcTemplate.queryForRowSet(sql);
        } else {
            rs = jdbcTemplate.queryForRowSet(sql, id);
        }
        while (rs.next()) {
            long filmId = rs.getLong("film_id");
            Film film = Film.builder()
                    .id(filmId)
                    .name(rs.getString("film_name"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .description(rs.getString("description"))
                    .duration(rs.getLong("duration"))
                    .rate(rs.getInt("rate"))
                    .build();
            Mpa mpa = Mpa.builder()
                    .id(rs.getLong("mpa_id"))
                    .name(rs.getString("mpa_name"))
                    .build();
            film.setMpa(mpa);
            Set<Genre> genres = new LinkedHashSet<>();

            SqlRowSet rsGenres = jdbcTemplate.queryForRowSet(GET_FILM_GENRES, filmId);
            while (rsGenres.next()) {
                Genre genre = Genre.builder()
                        .id(rsGenres.getLong("genre_id"))
                        .name(rsGenres.getString("genre_name"))
                        .build();
                genres.add(genre);
            }
            film.setGenres(genres);
            films.put(filmId, film);
        }
        return films;
    }

    private Set<Long> removeDoubles(Set<Genre> genres) {
        return genres.stream().map(Genre::getId).collect(Collectors.toSet());
    }

    private boolean updateFilmMpa(long mpaId, long filmId) {
        return jdbcTemplate.update(INSERT_FILM_MPA, filmId, mpaId) > 0;
    }

    private void updateFilmGenres(Set<Long> genreIds, long filmId) {
        genreIds.forEach(g -> jdbcTemplate.update(INSERT_FILM_GENRE, filmId, g));
    }

    private boolean deleteFilmMpa(Mpa mpa, long filmId) {
        return jdbcTemplate.update(DELETE_FILM_MPA, filmId, mpa.getId()) > 0;
    }

    private void deleteFilmGenres(Set<Genre> genres, long filmId) {
        genres.forEach(genre -> jdbcTemplate.update(DELETE_FILM_GENRE, filmId, genre.getId()));
    }
}