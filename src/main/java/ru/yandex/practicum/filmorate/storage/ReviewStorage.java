package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    List<Review> findFilmReviews(int filmId, int count);

    Review create(Review review);

    Review find(int id);

    Review update(Review review);

    void delete(int id);

    void setLikeToReview(int userId, int reviewId, int coefficient);

    void unsetLikeToReview(int userId, int reviewId);
}
