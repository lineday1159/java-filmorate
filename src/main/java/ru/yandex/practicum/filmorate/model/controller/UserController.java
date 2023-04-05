package ru.yandex.practicum.filmorate.model.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
public class UserController {

    private HashMap<Integer, User> users = new HashMap<>();
    private int currentId = 1;

    @GetMapping("/users")
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping(value = "/users")
    public User create(@Valid @RequestBody User user) {
        log.info("Получен post запрос к эндпоинту /users: '{}'", user.toString());
        if (user.getLogin().contains(" ")) {
            log.info("логин не может быть пустым и содержать пробелы");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        } else {
            if (user.getName() == null) {
                user.setName(user.getLogin());
            } else if (user.getName().isEmpty()) {
                user.setName(user.getLogin());
            }
            user.setId(currentId);
            users.put(currentId, user);
            currentId++;
            return user;
        }
    }

    @PutMapping(value = "/users")
    public User update(@Valid @RequestBody User user) {
        log.info("Получен put запрос к эндпоинту /users: '{}'", user.toString());
        if (users.containsKey(user.getId())) {
            User currentUser = users.get(user.getId());
            currentUser.setName(user.getName());
            currentUser.setBirthday(user.getBirthday());
            currentUser.setLogin(user.getLogin());
            currentUser.setEmail(user.getEmail());
        } else {
            log.info("Пользователь не найден с ID - {}",user.getId());
            throw new ValidationException("Пользователь не найден с ID - "+user.getId());
        }
        return users.get(user.getId());
    }
}