package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Review;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReviewServiceTests {

    @Autowired
    ReviewService testReviewService;

    @Test
    @Order(1)
    void getAllReviewsEmptyTest() {
        assertTrue(testReviewService.findFilmReviews(0, 10).isEmpty());
    }

    @Test
    @Order(2)
    void getReviewsToFilm1Test() {
        assertTrue(testReviewService.findFilmReviews(1, 10).isEmpty());
    }

    @Test
    @Order(3)
    void createReviewToFilm1Test() {
        Review testReview = testReviewService.create(
                new Review(
                        null,
                        "This film is soo bad.",
                        false,
                        1,
                        1,
                        null)
        );

        assertEquals(1,testReview.getReviewId());
    }
}
