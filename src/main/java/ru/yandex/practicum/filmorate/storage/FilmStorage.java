package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;


public interface FilmStorage {
    Film create(Film film);

    List<Film> findAll();

    Film update(Film film);

    Film find(Integer id);

    boolean delete(Integer id);

    Film deleteLikes(Integer filmId, Integer userId);

    List<Film> findCommonFilms(Integer userId, Integer friendId);
}
