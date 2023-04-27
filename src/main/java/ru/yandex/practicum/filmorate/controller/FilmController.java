package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/films")
    public List<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/films/{id}")
    public Film find(@PathVariable Integer id) {
        return filmService.find(id);
    }

    @PostMapping(value = "/films")
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен post запрос к эндпоинту /films: '{}'", film.toString());
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        } else {
            return filmService.create(film);
        }
    }

    @PutMapping(value = "/films")
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен put запрос к эндпоинту /films: '{}'", film.toString());
        return filmService.update(film);
    }

    @PutMapping(value = "/films/{id}/like/{userId}")
    public Film addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping(value = "/films/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping(value = "/films/popular")
    public List<Film> findPopFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.findPopFilms(count);
    }

    @GetMapping(value = "/films/common")
    public List<Film> getCommonFilm(@RequestParam Integer userId, @RequestParam Integer friendId) {
        return filmService.findCommonFriends(userId, friendId);
    }

    @DeleteMapping(value = "/films/{filmId}")
    public boolean delete(@PathVariable Integer filmId) {
        return filmService.delete(filmId);
    }
}
