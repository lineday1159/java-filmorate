package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.Entity;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private final UserStorage userStorage;
    @Autowired
    private final EventStorage eventStorage;

    public User addFriend(Integer id, Integer friendId) {
        User user = userStorage.find(id);
        User friend = userStorage.find(friendId);

        user.addFriend(friendId);
        eventStorage.addEvent(
                new Event(
                        Operation.ADD,
                        Entity.FRIEND,
                        id,
                        friendId
                )
        );
        return userStorage.update(user);
    }

    public User deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.find(userId);
        User friend = userStorage.find(friendId);

        userStorage.deleteFriend(userId, friendId);
        eventStorage.addEvent(
                new Event(
                        Operation.REMOVE,
                        Entity.FRIEND,
                        userId,
                        friendId
                )
        );
        return userStorage.find(userId);
    }

    public List<User> findFriends(Integer id) {
        User user = userStorage.find(id);
        Set<Integer> userFriendsId = user.getFriends();
        ArrayList<User> userFriends = new ArrayList<>();
        for (Integer friendId : userFriendsId) {
            userFriends.add(userStorage.find(friendId));
        }
        return userFriends;
    }

    public List<User> findCommonFriends(Integer id, Integer friendId) {
        User user = userStorage.find(id);
        User friend = userStorage.find(friendId);

        Set<Integer> commonFriendsId = new HashSet<>(user.getFriends());
        commonFriendsId.retainAll(friend.getFriends());

        ArrayList<User> userFriends = new ArrayList<>();
        for (Integer userId : commonFriendsId) {
            userFriends.add(userStorage.find(userId));
        }
        return userFriends;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }


    public User find(Integer id) {
        return userStorage.find(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public boolean delete(Integer id) {
        return userStorage.delete(id);
    }

    public List<Film> recommendations(Integer id) {
        return userStorage.recommendations(id);
    }

    public List<Event> getUserFeed(int userId) {
        userStorage.find(userId);
        return eventStorage.getUserFeed(userId);
    }
}
