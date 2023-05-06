package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorsStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("directorsDbStorage")
public class DirectorsDbStorage implements DirectorsStorage {

    private final JdbcTemplate jdbcTemplate;
    RowMapper<Director> rowMapper = (ResultSet resultSet, int rowNum) -> {
        return new Director(resultSet.getInt("id"), resultSet.getString("name"));
    };

    public DirectorsDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int create(Director director) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", director.getName());
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
        return (int) simpleJdbcInsert.executeAndReturnKey(values);
    }

    @Override
    public List<Director> findAll() {
        return jdbcTemplate.query("select * from directors order by id", rowMapper);
    }

    @Override
    public void update(Director director) {
        jdbcTemplate.update("update directors set name = ? where id = ?",
                director.getName(),
                director.getId());
    }

    @Override
    public Director find(Integer id) {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select name from directors where id = ?", id);
        if (directorRows.next()) {
            return new Director(id, directorRows.getString("name"));
        } else {
            log.info("Director не найден с ID - {}", id);
            throw new NotFoundException(String.format("Director с id-%d не существует.", id));
        }
    }

    @Override
    public boolean delete(Integer id) {
        return jdbcTemplate.update("delete from directors where id = ?", id) > 0;
    }

    public boolean exists(int id) {
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select id from directors " +
                "where id = ?", id);
        return directorRows.next();
    }
}
