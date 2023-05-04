package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorsService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final DirectorsService directorsService;

    @Autowired
    public FilmController(FilmService filmService, DirectorsService directorsService) {
        this.filmService = filmService;
        this.directorsService = directorsService;
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
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
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

    @GetMapping("/films/director/{directorId}")
    public List<Film> findFilmsByDirector(@RequestParam String sortBy, @PathVariable(required = true) int directorId) {
        if (!directorsService.exists(directorId)) {
            throw new NotFoundException(String.format("Режиссера с id-%d не существует.", directorId));
        }
        return filmService.findFilmsByDirector(sortBy, directorId);

    @DeleteMapping(value = "/films/{filmId}")
    public void delete(@PathVariable Integer filmId) {
        log.info("Получен delete запрос к эндпоинту /films/{}", filmId);
        if (!filmService.delete(filmId)) {
            log.info("В базе отсутствует фильм по данному ID-{}", filmId);
            throw new ValidationException("В базе отсутствует фильм по данному ID");
        }
    }
}