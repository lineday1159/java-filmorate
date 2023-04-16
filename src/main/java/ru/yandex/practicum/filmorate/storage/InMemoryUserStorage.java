package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private HashMap<Integer, User> users;
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
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User find(Integer id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        else {
            log.info("Пользователь не найден с ID - {}", id);
            throw new NotFoundException(String.format("Пользователя с id-\"%d\" не существует.", id));
        }
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            User currentUser = users.get(user.getId());
            currentUser.setName(user.getName());
            currentUser.setBirthday(user.getBirthday());
            currentUser.setLogin(user.getLogin());
            currentUser.setEmail(user.getEmail());
            return currentUser;
        } else {
            log.info("Пользователь не найден с ID - {}", user.getId());
            throw new NotFoundException(String.format("Пользователя с id-\"%d\" не существует.", user.getId()));
        }
    }
}
