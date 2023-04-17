package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final FilmStorage inMemoryFilmStorage;
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public FilmService(FilmStorage inMemoryFilmStorage, UserStorage inMemoryUserStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public Film addLike(Integer filmId, Integer userId) {
        Film film = inMemoryFilmStorage.find(filmId);
        User user = inMemoryUserStorage.find(userId);
        Set<Integer> filmLikesId = film.getLikes();
        filmLikesId.add(userId);
        film.setLikes(filmLikesId);
        return film;
    }

    public Film deleteLike(Integer filmId, Integer userId) {
        Film film = inMemoryFilmStorage.find(filmId);
        User user = inMemoryUserStorage.find(userId);
        Set<Integer> filmLikesId = film.getLikes();
        filmLikesId.remove(userId);
        film.setLikes(filmLikesId);
        return film;
    }

    public List<Film> findPopFilms(Integer size) {
        return inMemoryFilmStorage.findAll().stream()
                .sorted((p0, p1) -> compare(p0, p1))
                .limit(size)
                .collect(Collectors.toList());
    }

    private int compare(Film p0, Film p1) {
        return p1.getLikes().size() - p0.getLikes().size(); //прямой порядок сортировки
    }
}
