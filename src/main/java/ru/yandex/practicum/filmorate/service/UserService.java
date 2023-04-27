package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage inMemoryUserStorage) {
        this.userStorage = inMemoryUserStorage;
    }

    public User addFriend(Integer id, Integer friendId) {
        User user = userStorage.find(id);
        User friend = userStorage.find(friendId);

        user.addFriend(friendId);
        return userStorage.update(user);
    }

    public User deleteFriend(Integer id, Integer friendId) {
        User user = userStorage.find(id);
        User friend = userStorage.find(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(id);
        return userStorage.update(user);
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

    public boolean delete(Integer userId) {
        return userStorage.delete(userId);
    }
}
