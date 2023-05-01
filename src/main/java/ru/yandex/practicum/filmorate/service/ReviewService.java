package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

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

    public Review create(Review review) {
        if (review.getUserId() == null) throw new NotFoundException("Укажите id пользователя");
        if (review.getFilmId() == null) throw new NotFoundException("Укажите id фильма");
        if (review.getContent() == null || review.getContent().isBlank())
            throw new ValidationException("Напишите обЗор");
        if (review.getIsPositive() == null)
            throw new ValidationException("Указатеи положительный или отрицательный");
        review = reviewStorage.create(review);
        review.setUseful(0);
        return review;
    }

    public Review find(int id) {
        return reviewStorage.find(id);
    }



}
