package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.Duration;

@Data
public class Film {
    int id;
    @NotNull
    @NotBlank
    String name;
    @Size(max=200)
    String description;
    LocalDate releaseDate;
    @Positive
    int duration;
}
