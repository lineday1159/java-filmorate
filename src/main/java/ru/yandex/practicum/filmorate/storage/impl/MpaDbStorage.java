package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MpaDbStorage implements MpaStorage {

    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        String sql = "select * from mpa order by id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilmsMpa(rs));
    }

    @Override
    public Mpa find(Integer id) {
        String sql = "select * from mpa where id = ?";

        List<Mpa> mpaCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilmsMpa(rs), id);
        if (mpaCollection.size() == 1) {
            return mpaCollection.get(0);
        } else {
            log.info("mpa не найден с ID - {}", id);
            throw new NotFoundException(String.format("mpa с id-%d не существует.", id));
        }
    }

    private Mpa makeFilmsMpa(ResultSet rs) throws SQLException {
        Integer mpaId = rs.getInt("id");
        String mpaName = rs.getString("name");
        String mpaDescription = rs.getString("description");
        return new Mpa(mpaId, mpaName, mpaDescription);
    }


}
