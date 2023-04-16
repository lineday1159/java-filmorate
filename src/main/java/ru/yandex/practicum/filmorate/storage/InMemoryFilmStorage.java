package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("дата релиза — не раньше 28 декабря 1895 года");
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        } else {
            Integer id = implementId();
            film.setId(id);
            films.put(id, film);
            return film;
        }
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
            Film currentFilm = films.get(film.getId());
            currentFilm.setDescription(film.getDescription());
            currentFilm.setDuration(film.getDuration());
            currentFilm.setReleaseDate(film.getReleaseDate());
            currentFilm.setName(film.getName());
            return currentFilm;
        } else {
            log.info("фильм не найден с ID - {}", film.getId());
            throw new NotFoundException(String.format("Фильма с id-\"%d\" не существует.", film.getId()));
        }
    }
}
