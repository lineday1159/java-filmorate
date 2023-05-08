package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventDbStrorage implements EventStorage {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        log.trace("Level: Storage. Class EventDbStrorage. Call of addEvent method");
        SimpleJdbcInsert smplIns = new SimpleJdbcInsert(jdbcTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("event_timestamp", event.getTimestamp());
        values.put("event_type", event.getEventType());
        values.put("operation", event.getOperation());
        values.put("entity_id", event.getEntityId());
        values.put("user_id", event.getUserId());
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("event_log")
                .usingGeneratedKeyColumns("id");
        try {
            int id = simpleJdbcInsert.executeAndReturnKey(values).intValue();
            if (id > 0) {
                event.setEventTd(id);
            } else {
                throw new ValidationException("Событие в журнал не добавлено. Ошибка валидации записи");
            }
        } catch (DataAccessException e) {
            throw new ValidationException("Не удалось добавить событие в журнал. Причина: " + e.getMessage());
        }
    }

    @Override
    public List<Event> getUserFeed(int userId) {
        log.trace("Layer: Storage. Class EventDbStrorage. Call of getUserFeed");
        String sql = "SELECT * FROM events_log WHERE user_id = ?";
        return jdbcTemplate.query(sql,
                new Object[]{userId},
                new EventRowMapper());
    }

    private class EventRowMapper implements RowMapper<Event> {

        @Override
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Event(
                    rs.getInt("id"),
                    rs.getTimestamp("event_timestamp"),
                    Operation.valueOf(rs.getString("operation")),
                    Entity.valueOf(rs.getString("event_type")),
                    rs.getInt("user_id"),
                    rs.getInt("entity_id")
            );
        }
    }

}
