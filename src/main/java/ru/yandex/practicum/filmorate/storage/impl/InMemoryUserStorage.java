package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users;
    private Integer currentId = 0;

    public InMemoryUserStorage() {
        this.users = new HashMap<>();
    }

    private Integer implementId() {
        return ++currentId;
    }

    @Override
    public User create(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        } else if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        Integer id = implementId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public boolean delete(Integer id) {
        if (users.containsKey(id)) {
            users.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deleteFriend(Integer userId, Integer friendId) {
        return false;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User find(Integer id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            log.info("Пользователь не найден с ID - {}", id);
            throw new NotFoundException(String.format("Пользователя с id-\"%d\" не существует.", id));
        }
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        } else {
            log.info("Пользователь не найден с ID - {}", user.getId());
            throw new NotFoundException(String.format("Пользователя с id-\"%d\" не существует.", user.getId()));
        }
    }

    @Override
    public boolean exists(int id) {
        return false;
    }
}
