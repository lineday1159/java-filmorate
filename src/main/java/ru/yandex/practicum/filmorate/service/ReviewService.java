package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    ReviewStorage reviewStorage;

    public List<Review> findFilmReviews(int filmId, int count) {
        return reviewStorage.findFilmReviews(filmId, count);
    }

    public Review create(@Valid Review review) {
        return reviewStorage.create(review);
    }

}
