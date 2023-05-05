package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("mpa_id", film.getMpa().getId());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                values.put("director_id", director.getId());
            }
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        Integer filmId = simpleJdbcInsert.executeAndReturnKey(values).intValue();
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                log.info(filmId.toString() + '-' + genre.getId().toString());
                String sqlQuery = "insert into films_genres(film_id, genre_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(sqlQuery,
                        filmId,
                        genre.getId());
            }
        }
        return find(filmId);
    }


    @Override
    public List<Film> findAll() {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration,\n" +
                "f.director_id, d.name as director_name\n" +
                "from films f\n" +
                "JOIN mpa m ON m.id = f.mpa_id\n" +
                "LEFT JOIN directors d ON f.director_id = d.id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public boolean delete(Integer id) {
        String sqlQuery = "delete from films_likes where film_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        sqlQuery = "delete from films_genres where film_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        sqlQuery = "delete from films where id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public Film deleteLikes(Integer filmId, Integer userId) {
        jdbcTemplate.update("delete from films_likes where film_id = ? and user_id = ?", filmId, userId);
        return find(filmId);
    }

    @Override
    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        String usersLikesSql = "select * from films_likes where user_id = ?";
        Collection<Integer> usersFilmsId = jdbcTemplate.query(
                usersLikesSql,
                (rs, rowNum) ->
                        rs.getInt("film_id"), userId
        );
        String friendsLikesSql = "select * from films_likes where user_id = ?";
        Collection<Integer> friendsFilmsId = jdbcTemplate.query(
                friendsLikesSql,
                (rs, rowNum) ->
                        rs.getInt("film_id"), friendId
        );
        return usersFilmsId.stream()
                .filter(x -> friendsFilmsId.contains(x))
                .map(x -> find(x))
                .collect(Collectors.toList());
    }

    @Override
    public Film update(Film film) {
        find(film.getId());

        String sqlQuery = "update films set " +
                "name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?, director_id = ? " +
                "where id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getDirectors() != null ? film.getDirectors().get(0).getId() : null,
                film.getId());
        if (film.getGenres() != null) {
            String genreSqlQuery = "delete from films_genres where film_id = ?";
            jdbcTemplate.update(genreSqlQuery, film.getId());
            for (Genre genre : film.getGenres()) {
                genreSqlQuery = "insert into films_genres(film_id, genre_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(genreSqlQuery,
                        film.getId(),
                        genre.getId());
            }
        }
        return find(film.getId());
    }

    @Override
    public Film find(Integer id) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration,\n" +
                "f.director_id, d.name as director_name\n" +
                "from films f\n" +
                "JOIN mpa m ON m.id = f.mpa_id\n" +
                "LEFT JOIN directors d ON f.director_id = d.id\n" +
                "where f.id = ?";

        List<Film> filmCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
        if (filmCollection.size() == 1) {
            return filmCollection.get(0);
        } else {
            log.info("фильм не найден с ID - {}", id);
            throw new NotFoundException(String.format("Фильма с id-%d не существует.", id));
        }
    }

    private Film makeFilm(ResultSet rs) throws SQLException {

        Integer id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        Integer mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa_name");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");

        String genreSql = "select g.* \n" +
                "from films_genres f\n" +
                "JOIN genres g ON (f.genre_id = g.id)\n" +
                "where film_id = ?";
        List<Genre> genreCollection = jdbcTemplate.query(genreSql, (rs1, rowNum) -> makeFilmsGenre(rs1), id);

        String likesSql = "select * from films_likes where film_id = ?";
        List<Integer> usersCollection = jdbcTemplate.query(likesSql, (rs1, rowNum) -> makeFilmsLike(rs1), id);
        Film film = new Film(id, name, description, releaseDate, duration,
                new HashSet<>(genreCollection), new Mpa(mpaId, mpaName),
                new HashSet<>(usersCollection), new ArrayList<Director>());
        if (rs.getInt("director_id") > 0) {
            film.addDirector(new Director(rs.getInt("director_id"), rs.getString("director_name")));
        }
        return film;
    }

    private Genre makeFilmsGenre(ResultSet rs) throws SQLException {
        Integer genreId = rs.getInt("id");
        String genreName = rs.getString("name");
        return new Genre(genreId, genreName);
    }

    private Integer makeFilmsLike(ResultSet rs) throws SQLException {
        Integer userId = rs.getInt("user_id");
        return userId;
    }

    public void deleteDirectorFromFilms(int id) {
        jdbcTemplate.update("update films set director_id = null where director_id = ?", id);
    }

    public List<Film> getFilmsByDirectorByReleaseDate(int id) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration,\n" +
                "f.director_id, d.name as director_name\n" +
                "from films f\n" +
                "JOIN mpa m ON m.id = f.mpa_id\n" +
                "LEFT JOIN directors d ON f.director_id = d.id\n" +
                "where f.director_id = ?\n" +
                "order by extract(year from release_date), id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    public List<Film> getFilmsByDirectorByLikes(int id) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration,\n" +
                "f.director_id, d.name as director_name, count(fl.user_id) as likes\n" +
                "from films f\n" +
                "JOIN mpa m ON m.id = f.mpa_id\n" +
                "LEFT JOIN directors d ON f.director_id = d.id\n" +
                "LEFT JOIN films_likes fl on f.id = fl.film_id\n" +
                "where f.director_id = ?\n" +
                "group by id\n" +
                "order by likes";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("insert into films_likes(film_id, user_id) values (?, ?)", filmId, userId);
    }

    public boolean exits(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select exists(select id from films where id = ?) as exist", id);
        if (userRows.next()) {
            return userRows.getBoolean("exist");
        } else {
            return false;
        }
    }
}
