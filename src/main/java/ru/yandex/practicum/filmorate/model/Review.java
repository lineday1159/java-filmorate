package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class Review {

    private Integer reviewId;

    @NotBlank
    private String content;

    @NonNull
    private Boolean isPositive;

    @NonNull
    private Integer userId;

    @NonNull
    private Integer filmId;

    private Integer useful = 0;
}
