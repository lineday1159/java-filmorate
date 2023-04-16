package ru.yandex.practicum.filmorate.validation;

public class NotFoundException extends RuntimeException{
    public NotFoundException(final String message) {
        super(message);
    }
}
