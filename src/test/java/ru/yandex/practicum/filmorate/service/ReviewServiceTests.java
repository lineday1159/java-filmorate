package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.validation.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewServiceTests {

    @Autowired
    ReviewService testReviewService;

    @Test
    @Order(1)
    @Sql(scripts = {"classpath:testdata.sql"})
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

    @Test
    @Order(5)
    void createNullContentTest() {
        assertThrows(ValidationException.class, () -> {
            testReviewService.create(
                    new Review(
                            null,
                            null,
                            false,
                            1,
                            1,
                            0)
            );
        });
    }

    @Test
    @Order(6)
    void createNullIsPositiveTest() {
        assertThrows(NullPointerException.class, () -> {
            testReviewService.create(
                    new Review(
                            null,
                            "This film is soo bad.",
                            null,
                            1,
                            1,
                            0)
            );
        });
    }

    @Test
    @Order(7)
    void createNullUserTest() {
        assertThrows(NullPointerException.class, () -> {
            testReviewService.create(
                    new Review(
                            null,
                            "This film is soo bad.",
                            true,
                            null,
                            1,
                            0)
            );
        });
    }

    @Test
    @Order(7)
    void createFailUserTest() {
        assertThrows(NotFoundException.class, () -> {
            testReviewService.create(
                    new Review(
                            null,
                            "This film is soo bad.",
                            true,
                            -1,
                            1,
                            0)
            );
        });
    }

    @Test
    @Order(8)
    void createNullFilmTest() {
        assertThrows(NullPointerException.class, () -> {
            testReviewService.create(
                    new Review(
                            null,
                            "This film is soo bad.",
                            true,
                            1,
                            null,
                            0)
            );
        });
    }

    @Test
    @Order(9)
    void updateReview1Test() {
        Review testReview = testReviewService.update(
                new Review(
                        1,
                        "This film is soo bad.",
                        true,
                        2,
                        2,
                        0)
        );

        assertEquals(1,testReview.getReviewId());
        assertEquals("This film is soo bad.", testReview.getContent());
        assertEquals(true, testReview.getIsPositive());
        assertEquals(1, testReview.getUserId());
        assertEquals(1, testReview.getFilmId());
        assertEquals(0, testReview.getUseful());
    }

    @Test
    @Order(10)
    void findPositiveReviewWithId1Test() {
        Review testReview = testReviewService.find(1);

        assertEquals(1,testReview.getReviewId());
        assertEquals("This film is soo bad.", testReview.getContent());
        assertEquals(true, testReview.getIsPositive());
        assertEquals(1, testReview.getUserId());
        assertEquals(1, testReview.getFilmId());
        assertEquals(0, testReview.getUseful());
    }

    @Test
    @Order(11)
    void getReviewsToFilm1AfterUpdateTest() {
        List<Review> testReviews = testReviewService.findFilmReviews(1, 10);
        assertFalse(testReviews.isEmpty());
        assertEquals(1,testReviews.get(0).getReviewId());
        assertEquals("This film is soo bad.", testReviews.get(0).getContent());
        assertEquals(true, testReviews.get(0).getIsPositive());
        assertEquals(1, testReviews.get(0).getUserId());
        assertEquals(1, testReviews.get(0).getFilmId());
        assertEquals(0, testReviews.get(0).getUseful());
    }

    @Test
    @Order(12)
    void createAnotherReviewToFilm1Test() {
        Review testReview = testReviewService.create(
                new Review(
                        null,
                        "This film is soo bad.",
                        false,
                        2,
                        1,
                        0)
        );

        assertEquals(2,testReview.getReviewId());
        assertEquals("This film is soo bad.", testReview.getContent());
        assertEquals(false, testReview.getIsPositive());
        assertEquals(2, testReview.getUserId());
        assertEquals(1, testReview.getFilmId());
        assertEquals(0, testReview.getUseful());
    }

    @Test
    @Order(13)
    void getReviewsToFilm1AfterAddAnoterReviewTest() {
        List<Review> testReviews = testReviewService.findFilmReviews(1, 10);

        assertFalse(testReviews.isEmpty());
        assertEquals(2, testReviews.size());

        Review rev0 = testReviews.get(0);

        assertEquals(1,rev0.getReviewId());
        assertEquals("This film is soo bad.", rev0.getContent());
        assertEquals(true, rev0.getIsPositive());
        assertEquals(1, rev0.getUserId());
        assertEquals(1, rev0.getFilmId());
        assertEquals(0, rev0.getUseful());

        Review rev1 = testReviews.get(1);

        assertEquals(2,rev1.getReviewId());
        assertEquals("This film is soo bad.", rev1.getContent());
        assertEquals(false, rev1.getIsPositive());
        assertEquals(2, rev1.getUserId());
        assertEquals(1, rev1.getFilmId());
        assertEquals(0, rev1.getUseful());
    }

    @Test
    @Order(13)
    void getReviewsToFilm1AfterAddAnoterReviewWithCount1Test() {
        List<Review> testReviews = testReviewService.findFilmReviews(1, 1);

        assertFalse(testReviews.isEmpty());
        assertEquals(1, testReviews.size());

        assertEquals(1,testReviews.get(0).getReviewId());
        assertEquals("This film is soo bad.", testReviews.get(0).getContent());
        assertEquals(true, testReviews.get(0).getIsPositive());
        assertEquals(1, testReviews.get(0).getUserId());
        assertEquals(1, testReviews.get(0).getFilmId());
        assertEquals(0, testReviews.get(0).getUseful());
    }

    @Test
    @Order(14)
    void createGoodReviewToFilm2Test() {
        Review testReview = testReviewService.create(
                new Review(
                        null,
                        "This film is beatiful.",
                        true,
                        1,
                        2,
                        0)
        );

        assertEquals(3,testReview.getReviewId());
        assertEquals("This film is beatiful.", testReview.getContent());
        assertEquals(true, testReview.getIsPositive());
        assertEquals(1, testReview.getUserId());
        assertEquals(2, testReview.getFilmId());
        assertEquals(0, testReview.getUseful());
    }

    @Test
    @Order(15)
    void getReviewsWithCount3Test() {
        List<Review> testReviews = testReviewService.findFilmReviews(0, 3);

        assertFalse(testReviews.isEmpty());
        assertEquals(3, testReviews.size());

        Review rev1 = testReviews.get(1);
        Review rev0 = testReviews.get(0);
        Review rev2 = testReviews.get(2);

        assertEquals(1,rev0.getReviewId());
        assertEquals("This film is soo bad.", rev0.getContent());
        assertEquals(true, rev0.getIsPositive());
        assertEquals(1, rev0.getUserId());
        assertEquals(1, rev0.getFilmId());
        assertEquals(0, rev0.getUseful());

        assertEquals(2,rev1.getReviewId());
        assertEquals("This film is soo bad.", rev1.getContent());
        assertEquals(false, rev1.getIsPositive());
        assertEquals(2, rev1.getUserId());
        assertEquals(1, rev1.getFilmId());
        assertEquals(0, rev1.getUseful());

        assertEquals(3,rev2.getReviewId());
        assertEquals("This film is beatiful.", rev2.getContent());
        assertEquals(true, rev2.getIsPositive());
        assertEquals(1, rev2.getUserId());
        assertEquals(2, rev2.getFilmId());
        assertEquals(0, rev2.getUseful());
    }

    @Test
    @Order(16)
    void addLikeToReview2FromUSer1Test() {
        assertDoesNotThrow(() -> {
            testReviewService.likeReview(1,2);
        });
    }

    @Test
    @Order(17)
    void addDislikeToReview2FromUSer1Test() {
        assertDoesNotThrow(() -> {
            testReviewService.dislikeReview(2,1);
        });
    }

    @Test
    @Order(18)
    void getReviewsAfterLikesTest() {
        List<Review> testReviews = testReviewService.findFilmReviews(0, 10);

        assertFalse(testReviews.isEmpty());
        assertEquals(3, testReviews.size());

        Review rev0 = testReviews.get(0);
        Review rev1 = testReviews.get(1);
        Review rev2 = testReviews.get(2);

        assertEquals(1, rev0.getUseful());
        assertEquals(0, rev1.getUseful());
        assertEquals(-1, rev2.getUseful());

    }

    @Test
    @Order(19)
    void getReviewsToFilm1AfterLikesTest() {
        List<Review> testReviews = testReviewService.findFilmReviews(1, 10);

        assertFalse(testReviews.isEmpty());
        assertEquals(2, testReviews.size());

        assertEquals(1,testReviews.get(1).getReviewId());
        assertEquals("This film is soo bad.", testReviews.get(1).getContent());
        assertEquals(true, testReviews.get(1).getIsPositive());
        assertEquals(1, testReviews.get(1).getUserId());
        assertEquals(1, testReviews.get(1).getFilmId());
        assertEquals(-1, testReviews.get(1).getUseful());

        assertEquals(2,testReviews.get(0).getReviewId());
        assertEquals("This film is soo bad.", testReviews.get(0).getContent());
        assertEquals(false, testReviews.get(0).getIsPositive());
        assertEquals(2, testReviews.get(0).getUserId());
        assertEquals(1, testReviews.get(0).getFilmId());
        assertEquals(1, testReviews.get(0).getUseful());
    }

    @Test
    @Order(20)
    void deleteReviewTest() {
        assertDoesNotThrow(() -> {
            testReviewService.delete(1);
        });
    }

    @Test
    @Order(21)
    void getReview1AfterDeleteTest() {
        assertThrows(NotFoundException.class, () -> {
            testReviewService.find(1);
        });
    }

    @Test
    @Order(22)
    void getReviewsToFilm1AfterDeleteTest() {
        List<Review> testReviews = testReviewService.findFilmReviews(1, 10);

        assertFalse(testReviews.isEmpty());
        assertEquals(1, testReviews.size());

        assertEquals(2,testReviews.get(0).getReviewId());
        assertEquals("This film is soo bad.", testReviews.get(0).getContent());
        assertEquals(false, testReviews.get(0).getIsPositive());
        assertEquals(2, testReviews.get(0).getUserId());
        assertEquals(1, testReviews.get(0).getFilmId());
        assertEquals(1, testReviews.get(0).getUseful());
    }
}
