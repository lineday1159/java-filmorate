package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
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
