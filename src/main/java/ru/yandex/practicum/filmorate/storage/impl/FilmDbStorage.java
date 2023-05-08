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
    public boolean exists(int id) {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select id from films " +
                "where id = ?", id);
        return directorRows.next();
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
                String sqlQuery = "insert into films_genres(film_id, genre_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(sqlQuery,
                        filmId,
                        genre.getId());
            }
        }
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                String sqlQuery = "insert into films_directors(film_id, director_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(sqlQuery,
                        filmId,
                        director.getId());
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
                "left JOIN mpa m ON m.id = f.mpa_id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public List<Film> getFilmsByDirectorByReleaseDate(int id) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration,\n" +
                "from films f\n" +
                "LEFT JOIN mpa m ON m.id = f.mpa_id\n" +
                "JOIN films_directors d ON f.id = d.film_id\n" +
                "where d.director_id = ?\n" +
                "order by extract(year from release_date), id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    @Override
    public List<Film> getFilmsByDirectorByLikes(int id) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration,\n" +
                "count(fl.user_id) as likes\n" +
                "from films f\n" +
                "LEFT JOIN mpa m ON m.id = f.mpa_id\n" +
                "JOIN films_directors d ON f.id = d.film_id\n" +
                "LEFT JOIN films_likes fl on f.id = fl.film_id\n" +
                "where d.director_id = ?\n" +
                "group by id\n" +
                "order by likes";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
    }

    @Override
    public boolean delete(Integer id) {
        String sqlQuery = "delete from films where id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("insert into films_likes(film_id, user_id) values (?, ?)", filmId, userId);
    }

    @Override
    public Film deleteLikes(Integer filmId, Integer userId) {
        jdbcTemplate.update("delete from films_likes where film_id = ? and user_id = ?", filmId, userId);
        return find(filmId);
    }

    @Override
    public void deleteDirectorFromFilms(Integer filmId, Integer directorId) {
        jdbcTemplate.update("delete from films_directors where film_id = ? and director_id = ?", filmId, directorId);
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
        String genreSqlQuery = "delete from films_genres where film_id = ?";
        jdbcTemplate.update(genreSqlQuery, film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreSqlQuery = "insert into films_genres(film_id, genre_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(genreSqlQuery,
                        film.getId(),
                        genre.getId());
            }
        }
        String directorsSqlQuery = "delete from films_directors where film_id = ?";
        jdbcTemplate.update(directorsSqlQuery, film.getId());
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                directorsSqlQuery = "insert into films_directors(film_id, director_id) " +
                        "values (?, ?)";
                jdbcTemplate.update(directorsSqlQuery,
                        film.getId(),
                        director.getId());
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
                "LEFT JOIN mpa m ON m.id = f.mpa_id\n" +
                "where f.id = ?";

        List<Film> filmCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), id);
        if (filmCollection.size() == 1) {
            return filmCollection.get(0);
        } else {
            log.info("фильм не найден с ID - {}", id);
            throw new NotFoundException(String.format("Фильма с id-%d не существует.", id));
        }
    }

    @Override
    public List<Film> findFilmsByYearGenre(Optional<Integer> genreId, Optional<Integer> year) {
        String sql = "select f.id id, f.name name,f.description description,\n" +
                "f.mpa_id mpa_id, m.name as mpa_name,\n" +
                "f.release_date release_date, f.duration as duration\n" +
                "from films f\n" +
                "LEFT JOIN mpa m ON m.id = f.mpa_id\n" +
                ((genreId.isPresent()) ? "JOIN films_genres g ON g.film_id = f.id AND g.genre_id = ?\n" : "") +
                ((year.isPresent()) ? "WHERE EXTRACT(YEAR FROM f.release_date) = ?\n" : "");
        log.info(sql);
        List<Film> filmCollection;
        if (genreId.isPresent() && year.isPresent()) {
            filmCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId.get(), year.get());
        } else if (genreId.isPresent() && year.isEmpty()) {
            filmCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId.get());
        } else if (genreId.isEmpty() && year.isPresent()) {
            filmCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), year.get());
        } else {
            filmCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
        }
        return filmCollection;
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

        String directorSql = "select g.* \n" +
                "from films_directors f\n" +
                "JOIN directors g ON (f.director_id = g.id)\n" +
                "where film_id = ?";
        List<Director> directorCollection = jdbcTemplate.query(directorSql, (rs1, rowNum) -> makeFilmsDirectors(rs1), id);

        String likesSql = "select * from films_likes where film_id = ?";
        List<Integer> usersCollection = jdbcTemplate.query(likesSql, (rs1, rowNum) -> makeFilmsLike(rs1), id);

        return new Film(id, name, description, releaseDate, duration, new HashSet<>(genreCollection), new Mpa(mpaId, mpaName), new HashSet<>(usersCollection), new HashSet<>(directorCollection));
    }

    private Genre makeFilmsGenre(ResultSet rs) throws SQLException {
        Integer genreId = rs.getInt("id");
        String genreName = rs.getString("name");
        return new Genre(genreId, genreName);
    }

    private Director makeFilmsDirectors(ResultSet rs) throws SQLException {
        Integer directorId = rs.getInt("id");
        String directorName = rs.getString("name");
        return new Director(directorId, directorName);
    }

    private Integer makeFilmsLike(ResultSet rs) throws SQLException {
        Integer userId = rs.getInt("user_id");
        return userId;
    }

    @Override
    public List<Film> findFilm(String query, List<String> by) {
        StringBuilder sql = new StringBuilder("SELECT f.*, m.name as mpa_name, d.* " +
                "FROM films f " +
                "LEFT JOIN films_directors fd ON f.id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.id " +
                "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE ");

        if (by.size() == 1) {
            if (by.get(0).equals("director")) {
                sql.append("LOWER(d.name) LIKE LOWER(?)");
            } else {
                sql.append("LOWER(f.name) LIKE LOWER(?)");
            }
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> makeFilm(rs), "%" + query + "%");
        } else if (by.size() == 2) {
            sql.append("(LOWER(d.name) LIKE LOWER(?) OR LOWER(f.name) LIKE LOWER(?))");
            List<Film> result = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> makeFilm(rs), "%" + query + "%",
                    "%" + query + "%");
            return result.stream().distinct().collect(Collectors.toList());
        } else {
            return findAll();
        }
    }
}
