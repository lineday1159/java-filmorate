package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public User addFriend(Integer id, Integer friendId) {
        User user = inMemoryUserStorage.find(id);
        User friend = inMemoryUserStorage.find(friendId);

        user.addFriend(friendId);
        friend.addFriend(id);
        return user;
    }

    public User deleteFriend(Integer id, Integer friendId) {
        User user = inMemoryUserStorage.find(id);
        User friend = inMemoryUserStorage.find(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(id);
        return user;
    }

    public List<User> findFriends(Integer id) {
        User user = inMemoryUserStorage.find(id);
        Set<Integer> userFriendsId = user.getFriends();
        ArrayList<User> userFriends = new ArrayList<>();
        for (Integer friendId : userFriendsId) {
            userFriends.add(inMemoryUserStorage.find(friendId));
        }
        return userFriends;
    }

    public List<User> findCommonFriends(Integer id, Integer friendId) {
        User user = inMemoryUserStorage.find(id);
        User friend = inMemoryUserStorage.find(friendId);

        Set<Integer> commonFriendsId = new HashSet<Integer>(user.getFriends());
        commonFriendsId.retainAll(friend.getFriends());

        ArrayList<User> userFriends = new ArrayList<>();
        for (Integer userId : commonFriendsId) {
            userFriends.add(inMemoryUserStorage.find(userId));
        }
        return userFriends;
    }
}
