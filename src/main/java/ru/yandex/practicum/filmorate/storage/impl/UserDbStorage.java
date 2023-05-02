package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@Primary
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {

        String userName = user.getName();
        if (userName == null) {
            userName = user.getLogin();
        } else if (userName.isEmpty()) {
            userName = user.getLogin();
        }

        Map<String, Object> values = new HashMap<>();
        values.put("name", userName);
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("birthday", user.getBirthday());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        return find(simpleJdbcInsert.executeAndReturnKey(values).intValue());
    }

    @Override
    public List<User> findAll() {
        String sql = "select * from users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public User update(User user) {
        find(user.getId());

        String sqlQuery = "update users set " +
                "name = ?, login = ?, email = ?, birthday = ? " +
                "where id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());

        if (user.getFriends() != null) {
            String friendsSqlQuery = "delete from friendships where user_id = ?";
            jdbcTemplate.update(friendsSqlQuery, user.getId());
            for (Integer friendId : user.getFriends()) {
                friendsSqlQuery = "insert into friendships(user_id, friend_id, is_accepted) " +
                        "values (?, ?, ?)";
                jdbcTemplate.update(friendsSqlQuery,
                        user.getId(),
                        friendId,
                        false);
            }
        }

        return find(user.getId());
    }

    @Override
    public User find(Integer id) {

        String sql = "select * from users where id = ?";

        List<User> userCollection = jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
        if (userCollection.size() == 1) {
            return userCollection.get(0);
        } else {
            log.info("Пользователь не найден с ID - {}", id);
            throw new NotFoundException(String.format("Пользователя с id-%d не существует.", id));
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sqlQuery = "delete from friendships where friend_id = ? or user_id = ?";
        jdbcTemplate.update(sqlQuery, id, id);

        sqlQuery = "delete from users where id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    private User makeUser(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String login = rs.getString("login");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();

        String friendsSql = "select * from friendships where user_id = ?";
        List<Integer> friendsCollection = jdbcTemplate.query(friendsSql, (rs1, rowNum) -> makeUserFriend(rs1), id);

        return new User(id, name, email, login, birthday, new HashSet<Integer>(friendsCollection));
    }

    private Integer makeUserFriend(ResultSet rs) throws SQLException {
        Integer friendId = rs.getInt("friend_id");
        return friendId;
    }
}
