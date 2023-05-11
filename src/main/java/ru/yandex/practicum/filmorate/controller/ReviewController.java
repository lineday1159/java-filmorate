package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    @Autowired
    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    private final Review find(@PathVariable int reviewId, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        return reviewService.find(reviewId);
    }

    @GetMapping
    public List<Review> findFilmReviews(
            @RequestParam(defaultValue = "0") int filmId,
            @RequestParam(defaultValue = "10") int count,
            HttpServletRequest request
    ) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        return reviewService.findFilmReviews(filmId, count);
    }

    @PostMapping
    public Review create(@RequestBody @Valid Review review, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        return reviewService.update(review);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void likeReview(@PathVariable int reviewId,
                           @PathVariable int userId,
                           HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        reviewService.likeReview(userId, reviewId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void dislikeReview(@PathVariable int reviewId,
                              @PathVariable int userId,
                              HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        reviewService.dislikeReview(userId, reviewId);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable int reviewId, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        reviewService.delete(reviewId);
    }

    @DeleteMapping({"/{reviewId}/like/{userId}", "/{reviewId}/dislike/{userId}"})
    public void unlikeReview(@PathVariable int reviewId,
                             @PathVariable int userId,
                             HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        reviewService.unlikeReview(userId, reviewId);
    }
}
