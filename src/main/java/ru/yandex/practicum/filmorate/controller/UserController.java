package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    @Autowired
    private final UserService userService;

    @GetMapping
    public List<User> findAll() {
        log.info("Получен post запрос к эндпоинту /users");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User find(@PathVariable Integer id) {
        log.info("Получен post запрос к эндпоинту /users/{}", id);
        return userService.find(id);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getUserFeed(@PathVariable Integer id) {
        log.info("Получен post запрос к эндпоинту /users/{}/feed", id);
        return userService.getUserFeed(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен post запрос к эндпоинту /users: '{}'", user.toString());
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен put запрос к эндпоинту /users: '{}'", user.toString());
        return userService.update(user);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Получен put запрос к эндпоинту /users/{}/friends/{}", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("Получен delete запрос к эндпоинту /users/{}/friends/{}", id, friendId);
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping(value = "/{id}/friends")
    public List<User> getFriend(@PathVariable Integer id) {
        log.info("Получен get запрос к эндпоинту /users/{}/friends", id);
        return userService.findFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getCommonFriend(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.info("Получен get запрос к эндпоинту /users/{}/friends/common/{}", id, otherId);
        return userService.findCommonFriends(id, otherId);
    }

    @DeleteMapping(value = "/{userId}")
    public void delete(@PathVariable Integer userId) {
        log.info("Получен delete запрос к эндпоинту /users/{}", userId);
        if (!userService.delete(userId)) {
            log.info("В базе отсутствует пользователь по данному ID-{}", userId);
            throw new ValidationException("В базе отсутствует пользователь по данному ID");
        }
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> recommendations(@PathVariable Integer id) {
        log.info("Получен get запрос к эндпоинту /users/{}/recommendations", id);
        return userService.recommendations(id);
    }
}
