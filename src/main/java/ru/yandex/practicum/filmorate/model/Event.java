package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
public class Event {

    private Integer eventId = 0;

    @NotNull
    private Long timestamp;

    @NotNull
    private Operation operation;

    @NotNull
    private Entity eventType;

    @NotNull
    @Positive
    private Integer entityId;

    @NotNull
    @Positive
    private Integer userId;

    /*
     * Делаем конструктор с неполным набором аргументов, для того, чтобы проще создавать записи
     */
    public Event(Operation operation, Entity entity, int userId, int entityId) {
        if (userId <= 0 || entityId <= 0 || operation == null || entity == null) {
            throw new ValidationException("Некорректная сущность");
        }
        // Прямо в конструкторе зададим таймстамп
        this.timestamp = Instant.now().toEpochMilli();
        this.operation = operation;
        this.eventType = entity;
        this.entityId = entityId;
        this.userId = userId;
    }

}
