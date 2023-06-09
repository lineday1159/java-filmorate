package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String login;
    @Past
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();

    public User(Integer id, String name, String email, String login, LocalDate birthday, Set<Integer> friends) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        this.friends = friends;
    }

    public void addFriend(Integer friendId) {
        friends.add(friendId);
    }

    public void deleteFriend(Integer friendId) {
        friends.remove(friendId);
    }
}
