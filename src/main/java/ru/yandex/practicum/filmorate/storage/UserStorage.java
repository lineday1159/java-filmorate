package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    List<User> findAll();

    User update(User user);

    User find(Integer id);

    boolean delete(Integer id);

    List<Film> recommendations (Integer id);
}
