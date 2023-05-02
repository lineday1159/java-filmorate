package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
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
                "f.release_date release_date, f.duration as duration\n" +
                "from films f\n" +
                "JOIN mpa m ON m.id = f.mpa_id";

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
                "name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "where id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
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

        if (film.getLikes() != null) {
            String likeSqlQuery = "delete from films_likes where film_id = ?";
            jdbcTemplate.update(likeSqlQuery, film.getId());

            for (Integer userId : film.getLikes()) {
                likeSqlQuery = "insert into films_likes(film_id, user_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(likeSqlQuery,
                        film.getId(),
                        userId);
            }
        }


        return find(film.getId());
    }

    @Override
    public Film find(Integer id) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration\n" +
                "from films f\n" +
                "JOIN mpa m ON m.id = f.mpa_id\n" +
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

        return new Film(id, name, description, releaseDate, duration, new HashSet<>(genreCollection), new Mpa(mpaId, mpaName), new HashSet<>(usersCollection));
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
}
