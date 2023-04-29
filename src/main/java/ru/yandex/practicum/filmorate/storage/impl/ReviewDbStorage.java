package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Review> findAllFilmReviews(int filmId, int count) {
        log.trace("Call of findAllFilmReviews");
        String sql = "SELECT * FROM prepare_reviews"
                + (filmId == 0 ? "" : "WHERE film_id = " + filmId)
                + " LIMIT ?";
        return jdbcTemplate.query(
                sql,
                new Object[]{count},
                new ReviewRowMapper());
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
