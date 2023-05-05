package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.impl.DirectorsDbStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.List;

@Service
public class DirectorsService {

    private final DirectorsDbStorage storage;

    private final FilmService filmService;

    @Autowired
    public DirectorsService(@Qualifier("directorsDbStorage") DirectorsDbStorage storage, FilmService filmService) {
        this.storage = storage;
        this.filmService = filmService;
    }

    public Director create(Director director) {
        director.setId(storage.create(director));
        return director;
    }

    public List<Director> findAll() {
        return storage.findAll();
    }

    public void update(Director director) {
        if (!storage.directorExists(director.getId())) {
            throw new NotFoundException(String.format("Режиссера с id-%d не существует.", director.getId()));
        }
        storage.update(director);
    }

    public Director find(int id) {
        return storage.find(id);
    }

    public void delete(int id) {
        filmService.deleteDirectorFromFilms(id);
        storage.delete(id);
    }

    public boolean exists(int id) {
        return storage.directorExists(id);
    }
}