package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();

    UserDto createUser(UserDto userDto);

    UserDto findUserById(Long userId);

    UserDto updateUser(Long userId, UserDto userDto);

    void deleteUserById(Long userId);

}
