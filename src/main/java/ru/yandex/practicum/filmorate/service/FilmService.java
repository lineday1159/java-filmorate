package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorsStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final DirectorsStorage directorsStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage, DirectorsStorage directorsStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorsStorage = directorsStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!filmStorage.exists(filmId)) {
            throw new NotFoundException(String.format("Фильма с id-%d не существует.", filmId));
        }
        if (!userStorage.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id-%d не существует.", filmId));
        }
        filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        if (!filmStorage.exists(filmId)) {
            throw new NotFoundException(String.format("Фильма с id-%d не существует.", filmId));
        }
        if (!userStorage.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id-%d не существует.", filmId));
        }
        return filmStorage.deleteLikes(filmId, userId);
    }

    public List<Film> findPopFilms(Integer size) {
        return filmStorage.findAll().stream().sorted((p0, p1) -> compare(p0, p1)).limit(size).collect(Collectors.toList());
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


}
