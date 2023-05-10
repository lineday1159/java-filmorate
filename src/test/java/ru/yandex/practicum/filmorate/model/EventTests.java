package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

public class EventTests {

    // Создадим валидатор, для валидации полей
    static Validator validator;

    @BeforeAll
    static void setValidator() {
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void eventCreationTest() {

        // All args constructor
        assertDoesNotThrow(() -> {new Event(
                1,
                Instant.now().toEpochMilli(),
                Operation.ADD,
                Entity.LIKE,
                1,
                1
            );
        });

        // Custom constructor
        assertDoesNotThrow(
                () -> {new Event(
                        Operation.UPDATE,
                        Entity.FRIEND,
                        1,
                        1
                );
            }
        );

        assertThrows(ValidationException.class, () ->  {
                new Event(null, null, 0, 0);
            }
        );

        Set<ConstraintViolation<Event>> violation = validator.validate(
                    new Event(1, null, null, null, 0, 0)
                );
        assertFalse(violation.isEmpty());

    }

    @Test
    void eventUpdateingTest() {
        Event testEvent = new Event(
                1,
                Instant.now().toEpochMilli(),
                Operation.ADD,
                Entity.LIKE,
                1,
                1
        );

        assertTrue(validator.validate(testEvent).isEmpty());

        testEvent.setTimestamp(null);

        assertFalse(validator.validate(testEvent).isEmpty());
    }


}
