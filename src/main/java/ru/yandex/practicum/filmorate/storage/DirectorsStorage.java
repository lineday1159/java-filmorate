package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorsStorage {

    int create(Director director);

    List<Director> findAll();

    void update(Director director);

    Director find(Integer id);

    boolean delete(Integer id);
}
