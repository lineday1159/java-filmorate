package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventDbStrorage implements EventStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        Map<String, Object> values = new HashMap<>();
        values.put("event_timestamp", Timestamp.from(Instant.ofEpochMilli(event.getTimestamp())));
        values.put("event_type", event.getEventType());
        values.put("operation", event.getOperation());
        values.put("entity_id", event.getEntityId());
        values.put("user_id", event.getUserId());
        SimpleJdbcInsert smplIns = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events_log")
                .usingGeneratedKeyColumns("id");
        try {
            int id = smplIns.executeAndReturnKey(values).intValue();
            if (id > 0) {
                event.setEventId(id);
            } else {
                throw new ValidationException("Событие в журнал не добавлено. Ошибка валидации записи");
            }
        } catch (DataAccessException e) {
            throw new ValidationException("Не удалось добавить событие в журнал. Причина: " + e.getMessage());
        }
    }

    @Override
    public List<Event> getUserFeed(int userId) {
        String sql = "SELECT * FROM events_log WHERE user_id = ?";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> makeEvent(rs), userId);
    }

    private Event makeEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("id"),
                rs.getTimestamp("event_timestamp").toInstant().toEpochMilli(),
                Operation.valueOf(rs.getString("operation")),
                Entity.valueOf(rs.getString("event_type")),
                rs.getInt("entity_id"),
                rs.getInt("user_id")
        );
    }


}
