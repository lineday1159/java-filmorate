package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(scripts = {"classpath:testdata.sql"})
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
        List<Review> testReviews = testReviewService.findFilmReviews(1, 10);
        assertTrue(testReviews.isEmpty());
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
                        0)
        );

        assertEquals(1,testReview.getReviewId());
        assertEquals("This film is soo bad.", testReview.getContent());
        assertEquals(false, testReview.getIsPositive());
        assertEquals(1, testReview.getUserId());
        assertEquals(1, testReview.getFilmId());
        assertEquals(0, testReview.getUseful());
    }

    @Test
    @Order(4)
    void findReviewWithId1Test() {
        Review testReview = testReviewService.find(1);

        assertEquals(1,testReview.getReviewId());
        assertEquals("This film is soo bad.", testReview.getContent());
        assertEquals(false, testReview.getIsPositive());
        assertEquals(1, testReview.getUserId());
        assertEquals(1, testReview.getFilmId());
        assertEquals(0, testReview.getUseful());
    }
}
