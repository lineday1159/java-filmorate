package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.find(filmId);
        User user = userStorage.find(userId);
        Set<Integer> filmLikesId = film.getLikes();
        filmLikesId.add(userId);
        film.setLikes(filmLikesId);
        return filmStorage.update(film);
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        filmStorage.find(filmId);
        userStorage.find(userId);
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
        filmStorage.find(id);
        return filmStorage.delete(id);
    }
}
