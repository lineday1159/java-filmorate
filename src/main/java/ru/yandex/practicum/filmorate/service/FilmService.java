package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.impl.DirectorsDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final DirectorsDbStorage directorsDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmDbStorage filmStorage, @Qualifier("userDbStorage") UserDbStorage userStorage,
                       @Qualifier("directorsDbStorage") DirectorsDbStorage directorsDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorsDbStorage = directorsDbStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        if (userStorage.exists(userId) && filmStorage.exits(filmId)) {
            filmStorage.addLike(filmId, userId);
        }
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
        return filmStorage.delete(id);
    }

    public void deleteDirectorFromFilms(int id) {
        filmStorage.deleteDirectorFromFilms(id);
    }

    public List<Film> findFilmsByDirector(String sort, int id) {
        if (!directorsDbStorage.directorExists(id)) {
            throw new NotFoundException(String.format("Режиссера с id-%d не существует.", id));
        }
        if ("year".equals(sort)) {
            return filmStorage.getFilmsByDirectorByReleaseDate(id);
        } else {
            return filmStorage.getFilmsByDirectorByLikes(id);
        }
    }
}