package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorsService;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
public class DirectorsController {

    private final DirectorsService service;

    @Autowired
    public DirectorsController(DirectorsService service) {
        this.service = service;
    }

    @GetMapping("/directors")
    public List<Director> findAll() {
        return service.findAll();
    }

    @GetMapping("/directors/{id}")
    public Director find(@PathVariable int id) {
        return service.find(id);
    }

    @PostMapping(value = "/directors")
    Director create(@Valid @RequestBody Director director, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        log.info("Получен post запрос к эндпоинту /films: '{}'", director.toString());
        return service.create(director);
    }

    @PutMapping(value = "/directors")
    Director update(@Valid @RequestBody Director director, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        if (!service.exists(director.getId())) {
            throw new NotFoundException(String.format("Режиссера с id-%d не существует.", director.getId()));
        }
        service.update(director);
        return director;
    }

    @DeleteMapping("/directors/{id}")
    void delete(@PathVariable int id, HttpServletRequest request) {
        log.info("Получен запрос к эндпоинту: {}, Строка параметров запроса: {}", request.getRequestURI(), request.getQueryString());
        service.delete(id);
    }
}
