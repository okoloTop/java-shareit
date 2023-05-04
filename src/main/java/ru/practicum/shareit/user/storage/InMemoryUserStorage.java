package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long userId;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        long newUserId = getUserId();
        user.setId(newUserId);
        users.put(newUserId, user);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        checkUserExist(userId);
        return users.get(userId);
    }

    @Override
    public User updateUser(User user) {
        Long userId = user.getId();
        checkUserExist(userId);
        User updateUser = users.get(userId);
        if (user.getEmail() != null) {
            updateUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            updateUser.setName(user.getName());
        }
        users.put(userId, updateUser);
        return updateUser;
    }

    @Override
    public void deleteUserById(Long userId) {
        checkUserExist(userId);
        users.remove(userId);
    }

    @Override
    public boolean isUserByEmailExist(String email) {
        boolean resultFind = false;
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                resultFind = true;
                break;
            }
        }
        return resultFind;
    }

    private void checkUserExist(Long userId) {
        if (!users.containsKey(userId)) {
            throw new FoundException("Пользователь с таким id не найден");
        }
    }

    private long getUserId() {
        if (userId == null) {
            userId = 0L;
        }
        userId++;
        return userId;
    }
}
