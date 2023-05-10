package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    private final ReviewStorage reviewStorage;

    @Autowired
    private final EventStorage eventStorage;

    public List<Review> findFilmReviews(int filmId, int count) {
        return reviewStorage.findFilmReviews(filmId, count);
    }

    public Review create(Review review) {
        if (review.getContent() == null || review.getContent().isBlank())
            throw new ValidationException("Напишите обЗор");
        review = reviewStorage.create(review);
        review.setUseful(0);
        eventStorage.addEvent(
                new Event(
                        Operation.ADD,
                        Entity.REVIEW,
                        review.getUserId(),
                        review.getReviewId()
                )
        );
        return review;
    }

    public Review find(int id) {
        return reviewStorage.find(id);
    }

    public Review update(Review review) {
        if (review.getReviewId() == null)
            throw new NotFoundException(
                    String.format("Обзора с id-%d не существует.", review.getReviewId())
            );
        Review forUpdate = reviewStorage.update(review);
        eventStorage.addEvent(
                new Event(
                        Operation.UPDATE,
                        Entity.REVIEW,
                        review.getUserId(),
                        review.getReviewId()
                )
        );
        return forUpdate;
    }

    public void delete(int id) {
        int reviewUser = reviewStorage.find(id).getUserId();
        reviewStorage.delete(id);
        eventStorage.addEvent(
                new Event(
                        Operation.REMOVE,
                        Entity.REVIEW,
                        reviewUser,
                        reviewUser
                )
        );
    }

    public void likeReview(int userId, int reviewId) {
        reviewStorage.setLikeToReview(userId, reviewId, 1);
    }

    public void dislikeReview(int userId, int reviewId) {
        reviewStorage.setLikeToReview(userId, reviewId, -1);
    }

    public void unlikeReview(int userId, int reviewId) {
        reviewStorage.unsetLikeToReview(userId, reviewId);
    }

}
