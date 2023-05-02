package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    @Autowired
    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    private final Review find(@PathVariable int reviewId) {
        return reviewService.find(reviewId);
    }

    @GetMapping
    public List<Review> findFilmReviews(
            @RequestParam(defaultValue = "0") int filmId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return reviewService.findFilmReviews(filmId, count);
    }

    @PostMapping
    public Review create(@RequestBody @Valid Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review) {
        return reviewService.update(review);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void likeReview(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.likeReview(userId, reviewId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void dislikeReview(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.dislikeReview(userId, reviewId);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable int reviewId) {
        reviewService.delete(reviewId);
    }

    @DeleteMapping({"/{reviewId}/like/{userId}","/{reviewId}/dislike/{userId}"})
    public void unlikeReview(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.unlikeReview(userId, reviewId);
    }
}
