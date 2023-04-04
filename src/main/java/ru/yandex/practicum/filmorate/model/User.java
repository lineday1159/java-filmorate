package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    int id;
    String name;
    @NotNull
    @NotBlank
    @Email
    String email;
    @NotNull
    @NotBlank
    String login;
    @Past
    LocalDate birthday;
}
