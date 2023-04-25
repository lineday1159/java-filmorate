package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GenreDbStorage implements GenreStorage {
    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        String sql = "select * from genres order by id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilmsGenre(rs));
    }

    @Override
    public Genre find(Integer id) {
        String sql = "select * from genres where id = ?";

        List<Genre> genreCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilmsGenre(rs), id);
        if (genreCollection.size() == 1) {
            return genreCollection.get(0);
        } else {
            log.info("genre не найден с ID - {}", id);
            throw new NotFoundException(String.format("genre с id-%d не существует.", id));
        }
    }

    private Genre makeFilmsGenre(ResultSet rs) throws SQLException {
        Integer genreId = rs.getInt("id");
        String genreName = rs.getString("name");
        return new Genre(genreId, genreName);
    }

}
