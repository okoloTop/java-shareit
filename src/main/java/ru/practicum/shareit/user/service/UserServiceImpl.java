package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findAll() {
        return userListToDto(userStorage.findAll());
    }

    private List<UserDto> userListToDto(List<User> userList) {
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            userDtoList.add(userMapper.userToDto(user));
        }
        return userDtoList;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new InvalidParameterException("Пустой адрес электронной почты");
        }
        if (userStorage.isUserByEmailExist(userDto.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        User user = userStorage.createUser(userMapper.dtoToUser(userDto));
        return userMapper.userToDto(user);
    }

    @Override
    public UserDto findUserById(Long userId) {
        return userMapper.userToDto(userStorage.getUserById(userId));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        if (userStorage.getUserById(userId) == null) {
            throw new FoundException("Такого пользователя нет в базе");
        }
        if (userStorage.isUserByEmailExist(userDto.getEmail()) && !userStorage.getUserById(userId).getEmail().equals(userDto.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        userDto.setId(userId);
        User user = userStorage.updateUser(userMapper.dtoToUser(userDto));
        return userMapper.userToDto(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }
}
