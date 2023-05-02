package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @GetMapping("/{reviewId}")
    Review find(@PathVariable int reviewId) {
        return reviewService.find(reviewId);
    }

    @GetMapping
    List<Review> findFilmReviews(
            @RequestParam(defaultValue = "0") int filmId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return reviewService.findFilmReviews(filmId, count);
    }

    @PostMapping
    Review create(@RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    Review update(@RequestBody Review review) {
        return reviewService.update(review);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    void likeReview(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.likeReview(userId, reviewId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    void dislikeReview(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.dislikeReview(userId, reviewId);
    }

    @DeleteMapping("/{reviewId}")
    void delete(@PathVariable int reviewId) {
        reviewService.delete(reviewId);
    }

    @DeleteMapping({"/{reviewId}/like/{userId}","/{reviewId}/dislike/{userId}"})
    void unlikeReview(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.unlikeReview(userId, reviewId);
    }
}
