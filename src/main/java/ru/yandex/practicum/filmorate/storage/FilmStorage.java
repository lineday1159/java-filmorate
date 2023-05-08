package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;


public interface FilmStorage {
    Film create(Film film);

    List<Film> findAll();

    Film update(Film film);

    Film find(Integer id);

    boolean delete(Integer id);

    Film deleteLikes(Integer filmId, Integer userId);

    void addLike(Integer filmId, Integer userId);

    List<Film> findCommonFilms(Integer userId, Integer friendId);

    void deleteDirectorFromFilms(Integer filmId, Integer directorId);

    List<Film> getFilmsByDirectorByReleaseDate(int id);

    List<Film> getFilmsByDirectorByLikes(int id);

    List<Film> findFilm(String query, List<String> by);

    List<Film> findFilmsByYearGenre(Optional<Integer> genreId, Optional<Integer> year);


    boolean exists(int id);
}
