package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public User find(@PathVariable Integer id) {
        return userService.find(id);
    }

    @PostMapping(value = "/users")
    public User create(@Valid @RequestBody User user) {
        log.info("Получен post запрос к эндпоинту /users: '{}'", user.toString());
        return userService.create(user);
    }

    @PutMapping(value = "/users")
    public User update(@Valid @RequestBody User user) {
        log.info("Получен put запрос к эндпоинту /users: '{}'", user.toString());
        return userService.update(user);
    }

    @PutMapping(value = "/users/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping(value = "/users/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping(value = "/users/{id}/friends")
    public List<User> getFriend(@PathVariable Integer id) {
        return userService.findFriends(id);
    }

    @GetMapping(value = "/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriend(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.findCommonFriends(id, otherId);
    }

    @DeleteMapping(value = "/users/{userId}")
    public void delete(@PathVariable Integer userId) {
        log.info("Получен delete запрос к эндпоинту /users/{}", userId);
        if (!userService.delete(userId)) {
            log.info("В базе отсутствует пользователь по данному ID-{}", userId);
            throw new ValidationException("В базе отсутствует пользователь по данному ID");
        }
    }

    @GetMapping("/users/{id}/recommendations")
    public List<Film> recommendations(@PathVariable Integer id) {
        return userService.recommendations(id);
    }
}
