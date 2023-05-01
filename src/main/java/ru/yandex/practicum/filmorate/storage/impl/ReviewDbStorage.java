package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.Valid;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Review> findFilmReviews(int filmId, int count) {
        log.trace("Layer: Storage. Call of findAllFilmReviews");
        String sql = "SELECT * FROM prepare_reviews"
                + (filmId == 0 ? "" : " WHERE film_id = " + filmId)
                + " LIMIT ?";
        log.debug("SQL = " + sql);
        return jdbcTemplate.query(
                sql,
                new Object[]{count},
                new ReviewRowMapper());
    }

    @Override
    public Review create(@Valid Review review) {
        log.trace("Layer: Storage. Call of create review");

        Map<String, Object> values = new HashMap<>();
        values.put("content", review.getContent());
        values.put("is_positive", review.getIsPositive());
        values.put("user_id", review.getUserId());
        values.put("film_id", review.getFilmId());
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("id");
        try {
            int id = simpleJdbcInsert.executeAndReturnKey(values).intValue();
            if (id > 0) {
                review.setReviewId(id);
            } else {
                throw new ValidationException("Не удалось добавить обзор.");
            }
        } catch (DataAccessException e) {
            throw new ValidationException("Не удалось добавить обзор. Причина: " + e.getMessage());
        }


        return review;
    }

    @Override
    public Review find(int id) {
        log.trace("Layer: Storage. Call of find");
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM prepare_reviews WHERE id = ?",
                    new Object[]{id},
                    new ReviewRowMapper());
        } catch (DataAccessException e) {
            throw new NotFoundException(String.format("Обзора с id-%d не существует.", id));
        }
    }

    private class ReviewRowMapper implements RowMapper<Review> {

        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Review(
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getBoolean("is_positive"),
                    rs.getInt("user_id"),
                    rs.getInt("film_id"),
                    rs.getInt("useful")
            );
        }
    }

}
