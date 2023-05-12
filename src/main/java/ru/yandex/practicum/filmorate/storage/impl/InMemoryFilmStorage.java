package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Integer, Film> films;
    private Integer currentId = 0;

    public InMemoryFilmStorage() {
        this.films = new HashMap<>();
    }

    private Integer implementId() {
        return ++currentId;
    }

    @Override
    public Film create(Film film) {
        Integer id = implementId();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film find(Integer id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            log.info("фильм не найден с ID - {}", id);
            throw new NotFoundException(String.format("Фильма с id-\"%d\" не существует.", id));
        }
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        } else {
            log.info("фильм не найден с ID - {}", film.getId());
            throw new NotFoundException(String.format("Фильма с id-\"%d\" не существует.", film.getId()));
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (films.containsKey(id)) {
            films.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        return null;
    }

    @Override
    public Film deleteLike(Integer filmId, Integer userId) {
        return null;
    }

    @Override
    public void deleteDirectorFromFilms(Integer filmId, Integer directorId) {

    }

    @Override
    public List<Film> getFilmsByDirectorByReleaseDate(int id) {
        return null;
    }

    @Override
    public List<Film> getFilmsByDirectorByLikes(int id) {
        return null;
    }

    @Override
    public List<Film> findFilm(String query, List<String> by) {
        return null;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
    }

    @Override
    public List<Film> findFilmsByYearGenre(Optional<Integer> genreId, Optional<Integer> year) {
        return null;
    }

    @Override
    public boolean exists(int id) {
        return false;
    }

    @Override
    public List<Film> recommendations(Integer id) {
        return null;
    }
}
