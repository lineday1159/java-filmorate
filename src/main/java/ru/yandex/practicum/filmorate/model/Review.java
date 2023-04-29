package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class Review {

    private Integer reviewId;

    @NotNull
    @NotBlank
    private String content;

    private boolean isPositive;

    private Integer userId;

    private Integer filmId;

    private int useful = 0;
}
