package ru.yandex.practicum.filmorate.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
@Primary
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    private final JdbcTemplate jdbcTemplate;

    private FilmStorage filmStorage;

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

    @Override
    public List<Film> recommendations(Integer id) {
        List<Film> result = new ArrayList<>();
        Map<Integer, Set<Integer>> likes = getLikes();
        /*Если для данного пользователя нет оценок, или количество пользователей,
        которые поставили оценки меньше или равно 1, то возвращаем пустой список фильмов*/
        if (!likes.containsKey(id) || likes.size() <= 1) {
            return result;
        }
        // Получаем множество фильмов, которые оценил пользователь с заданным id
        Set<Integer> targetSet = likes.get(id);
        // Удаляем пользователя с заданным id из Map
        likes.remove(id);
        // Ищем пользователя, чьи оценки имеют максимальное пересечение с оценками пользователя с заданным id
        int maxIntersectionSize = 0;
        Integer recommendUserId = null;
        for (Map.Entry<Integer, Set<Integer>> uid : likes.entrySet()) {
            // Если множества оценок двух пользователей равны, то пропускаем эту итерацию цикла
            if (targetSet.equals(uid.getValue())) {
                continue;
            }
            // Находим пересечение множеств оценок двух пользователей
            Set<Integer> intersection = new HashSet<>(uid.getValue());
            intersection.retainAll(targetSet);
            /* Если пересечение множеств больше, чем предыдущее максимальное пересечение, то обновляем
            максимальное пересечение и id пользователя
             */
            if (intersection.size() > maxIntersectionSize) {
                maxIntersectionSize = intersection.size();
                recommendUserId = uid.getKey();
            }
        }
        // Если нашли пользователя, чьи оценки имеют наибольшее пересечение с оценками пользователя с заданным id
        if (recommendUserId != null) {
            /* Получаем множество фильмов, которые оценил найденный пользователь, но которые пользователь
            с заданным id не оценил
             */
            Set<Integer> set = new HashSet<>(likes.get(recommendUserId));
            set.removeAll(targetSet);
            /* Для каждого фильма в множестве находим объект фильма в хранилище фильмов и
            добавляем его в результирующий список
             */
            for (Integer filmId : set) {
                result.add(filmStorage.find(filmId));
            }
        }
        return result;
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

    private Map<Integer, Set<Integer>> getLikes() {
        // Создаем пустой Map для хранения оценок пользователей
        Map<Integer, Set<Integer>> likes = new HashMap<>();
        // Выполняем запрос к базе данных, чтобы получить id пользователей и фильмов, которые они оценили
        jdbcTemplate.query("select film_id, user_id from films_likes",
                (rs, rowNum) -> {
                    Integer userId = rs.getInt("user_id");
                    Integer filmId = rs.getInt("film_id");
                    // Если пользователь еще не добавлен в Map, то добавляем его
                    if (!likes.containsKey(userId)) {
                        likes.put(userId, new HashSet<>());
                    }
                    // Добавляем оценку пользователя для данного фильма в его множество оценок
                    likes.get(userId).add(filmId);
                    return null;
                });
        // Возвращаем Map с оценками пользователей
        return likes;
    }

    private Integer makeUserFriend(ResultSet rs) throws SQLException {
        Integer friendId = rs.getInt("friend_id");
        return friendId;
    }
}
