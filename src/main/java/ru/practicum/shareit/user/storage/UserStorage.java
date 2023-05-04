package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();

    User createUser(User user);

    User getUserById(Long userId);

    User updateUser(User user);

    void deleteUserById(Long userId);

    boolean isUserByEmailExist(String email);
}

