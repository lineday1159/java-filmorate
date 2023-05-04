package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
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

    @Autowired
    private final UserStorage userStorage;

    @Autowired
    private final FilmStorage filmStorage;

    @Override
    public List<Review> findFilmReviews(int filmId, int count) {
        log.trace("Layer: Storage. Call of findAllFilmReviews");
        String sql = "SELECT * FROM prepare_reviews"
                + (filmId == 0 ? "" : " WHERE film_id = " + filmId)
                + " ORDER BY useful DESC, id LIMIT ?";
        log.debug("SQL = " + sql);
        return jdbcTemplate.query(
                sql,
                new Object[]{count},
                new ReviewRowMapper());
    }

    @Override
    public Review create(@Valid Review review) {
        log.trace("Layer: Storage. Call of create review");
        /*
            Как-то так, ошибку от sql пока по-человечески не разобрать.
            При попытке закинуть с некорректными id он выдаёт ошибку и
            всё равно увеличивает инкремент, и тесты не проходят.
        */
        userStorage.find(review.getUserId());
        filmStorage.find(review.getFilmId());
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
        String sql = "SELECT * FROM PREPARE_REVIEWS WHERE ID = ?";
        Review review = null;
        try {
             review = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{id},
                    new ReviewRowMapper());
        } catch (DataAccessException e) {
            throw new NotFoundException(String.format("Обзора с id-%d не существует.", id));
        }
        return review;
    }

    @Override
    public Review update(@Valid Review review) {
        log.trace("Layer: Storage. Call of update");
        String sql = "UPDATE REVIEWS SET "
                + "CONTENT = ?, IS_POSITIVE = ?"
                + "WHERE ID = ?";
        int updated = jdbcTemplate.update(
                sql,
                new Object[]{
                    review.getContent(),
                    review.getIsPositive(),
                    review.getReviewId()
        });
        if (updated == 0) {
            throw new NotFoundException(String.format("Обзора с id-%d не существует.", review.getReviewId()));
        }
        return find(review.getReviewId());
    }

    @Override
    public void delete(int id) {
        log.trace("Layer: Storage. Call of delete");
        String sql = "DELETE FROM REVIEWS WHERE ID = ?";
        int updated = jdbcTemplate.update(
                sql,
                new Object[]{id});
        if (updated == 0) {
            throw new NotFoundException(String.format("Обзора с id-%d не существует.", id));
        }
    }

    @Override
    public void setLikeToReview(int userId, int reviewId, int coefficient) {
        log.trace("Layer: Storage. Call of setLikeToReview with coefficient = " + coefficient);
        String sql = "MERGE INTO review_likes (USER_ID, REVIEW_ID, WAS_USEFULL) " +
                "KEY(USER_ID, REVIEW_ID) VALUES (?, ?, ?)";
        int wasUsefull = coefficient / Math.abs(coefficient); // приведём к 1 на всякий
        try {
            jdbcTemplate.update(sql, userId, reviewId, wasUsefull);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь или обзор не найдены.");
        }
    }

    @Override
    public void unsetLikeToReview(int userId, int reviewId) {
        log.trace("Layer: Storage. Call of unsetLikeToReview");
        String sql = "DELETE FROM review_likes WHERE user_id = ? AND review_id = ?";
        try {
            jdbcTemplate.update(sql, userId, reviewId);
        } catch (DataAccessException e) {
            throw new NotFoundException(
                    String.format("Пользователь %d не ставил оценку обзору %d", userId, reviewId));
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
