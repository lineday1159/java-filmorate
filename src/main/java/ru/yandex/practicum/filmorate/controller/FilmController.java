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
import java.util.Optional;

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
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping(value = "/films/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping(value = "/films/common")
    public List<Film> getCommonFilm(@RequestParam Integer userId, @RequestParam Integer friendId) {
        return filmService.findCommonFriends(userId, friendId);
    }

    @GetMapping("/films/director/{directorId}")
    public List<Film> findFilmsByDirector(@RequestParam String sortBy, @PathVariable(required = true) int directorId) {
        return filmService.findFilmsByDirector(sortBy, directorId);
    }

    @GetMapping(value = "/films/popular")
    public List<Film> getMostPopularFilm(@RequestParam(defaultValue = "10") Optional<Integer> count, @RequestParam Optional<Integer> genreId, @RequestParam Optional<Integer> year) {
        log.info("Получен get запрос к эндпоинту /films/popular count={}&genreId={}&year={}", count.get(), genreId.isPresent() ? genreId.get() : "null", year.isPresent() ? year.get() : "null");
        return filmService.findMostPopularFilms(count, genreId, year);
    }

    @DeleteMapping(value = "/films/{filmId}")
    public void delete(@PathVariable Integer filmId) {
        log.info("Получен delete запрос к эндпоинту /films/{}", filmId);
        if (!filmService.delete(filmId)) {
            log.info("В базе отсутствует фильм по данному ID-{}", filmId);
            throw new ValidationException("В базе отсутствует фильм по данному ID");
        }
    }

    @GetMapping(value = "/films/search")
    public List<Film> findFilm(@RequestParam(required = false) String query,
                               @RequestParam(required = false) List<String> by) {
        return filmService.findFilm(query, by);
    }
}
