package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.DirectorsStorage;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Autowired
    private final FilmStorage filmStorage;
    @Autowired
    private final UserStorage userStorage;
    @Autowired
    private final EventStorage eventStorage;
    @Autowired
    private final DirectorsStorage directorsStorage;


    public void addLike(Integer filmId, Integer userId) {
        if (!filmStorage.exists(filmId)) {
            throw new NotFoundException(String.format("Фильма с id-%d не существует.", filmId));
        }
        if (!userStorage.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id-%d не существует.", filmId));
        }
        filmStorage.addLike(filmId, userId);
        eventStorage.addEvent(
                new Event(
                        Operation.ADD,
                        Entity.LIKE,
                        userId,
                        filmId
                )
        );
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        if (!filmStorage.exists(filmId)) {
            throw new NotFoundException(String.format("Фильма с id-%d не существует.", filmId));
        }
        if (!userStorage.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id-%d не существует.", filmId));
        }
        Film delFilm = filmStorage.deleteLike(filmId, userId);
        eventStorage.addEvent(
                new Event(
                        Operation.REMOVE,
                        Entity.LIKE,
                        userId,
                        filmId
                )
        );
        return delFilm;
    }

    public List<Film> findPopFilms(Integer size) {
        return filmStorage.findAll().stream().sorted((p0, p1) -> compare(p0, p1)).limit(size).collect(Collectors.toList());
    }

    public List<Film> findMostPopularFilms(Optional<Integer> count, Optional<Integer> genreId, Optional<Integer> year) {
        return filmStorage.findFilmsByYearGenre(genreId, year).stream().sorted((p0, p1) -> compare(p0, p1)).limit(count.get()).collect(Collectors.toList());
    }

    private int compare(Film p0, Film p1) {
        return p1.getLikes().size() - p0.getLikes().size(); //прямой порядок сортировки
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film find(Integer id) {
        return filmStorage.find(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public List<Film> findCommonFriends(Integer userId, Integer friendId) {
        return filmStorage.findCommonFilms(userId, friendId).stream().sorted(this::compare).collect(Collectors.toList());
    }

    public boolean delete(Integer id) {
        return filmStorage.delete(id);
    }

    public void deleteDirectorFromFilms(Integer filmId, Integer directorId) {
        filmStorage.deleteDirectorFromFilms(filmId, directorId);
    }

    public List<Film> findFilmsByDirector(String sort, int id) {
        if (!directorsStorage.exists(id)) {
            throw new NotFoundException(String.format("Режиссера с id-%d не существует.", id));
        }
        if ("year".equals(sort)) {
            return filmStorage.getFilmsByDirectorByReleaseDate(id);
        } else {
            return filmStorage.getFilmsByDirectorByLikes(id);
        }
    }

    public List<Film> findFilm(String query, List<String> by) {
        return filmStorage.findFilm(query, by).stream().sorted(this::compare).collect(Collectors.toList());
    }
}
