package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    @Autowired
    private final FilmService filmService;


    @GetMapping
    public List<Film> findAll() {
        log.info("Получен get запрос к эндпоинту /films");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film find(@PathVariable Integer id) {
        log.info("Получен get запрос к эндпоинту /films/{}", id);
        return filmService.find(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен post запрос к эндпоинту /films: '{}'", film.toString());
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        } else {
            return filmService.create(film);
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен put запрос к эндпоинту /films: '{}'", film.toString());
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен put запрос к эндпоинту /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен delete запрос к эндпоинту /films/{}/like/{}", id, userId);
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilm(@RequestParam Integer userId, @RequestParam Integer friendId) {
        log.info("Получен get запрос к эндпоинту /films/common");
        return filmService.findCommonFriends(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> findFilmsByDirector(@RequestParam String sortBy, @PathVariable(required = true) int directorId) {
        log.info("Получен get запрос к эндпоинту /films/director/{}", directorId);
        return filmService.findFilmsByDirector(sortBy, directorId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilm(@RequestParam(defaultValue = "10") Optional<Integer> count, @RequestParam Optional<Integer> genreId, @RequestParam Optional<Integer> year) {
        log.info("Получен get запрос к эндпоинту /films/popular count={}&genreId={}&year={}", count.get(), genreId.isPresent() ? genreId.get() : "null", year.isPresent() ? year.get() : "null");
        return filmService.findMostPopularFilms(count, genreId, year);
    }

    @DeleteMapping("/{filmId}")
    public void delete(@PathVariable Integer filmId) {
        log.info("Получен delete запрос к эндпоинту /films/{}", filmId);
        if (!filmService.delete(filmId)) {
            log.info("В базе отсутствует фильм по данному ID-{}", filmId);
            throw new ValidationException("В базе отсутствует фильм по данному ID");
        }
    }

    @GetMapping("/search")
    public List<Film> findFilm(@RequestParam(required = false) String query,
                               @RequestParam(required = false) List<String> by) {
        log.info("Получен get запрос к эндпоинту /films/search");
        return filmService.findFilm(query, by);
    }
}
