package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> findAll() {
        return userListToDto(userRepository.findAll());
    }

    private List<UserDto> userListToDto(List<User> userList) {
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            userDtoList.add(UserMapper.userToDto(user));
        }
        return userDtoList;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new InvalidParameterException("Пустой адрес электронной почты");
        }
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new InvalidParameterException("Пустой адрес электронной почты");
        }
        User user = userRepository.save(UserMapper.dtoToUser(userDto));
        return UserMapper.userToDto(user);
    }

    @Override
    public UserDto findUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new FoundException("Такого пользователя нет в базе");
        }
        return UserMapper.userToDto(user.get());
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User updateUser = userRepository.findUserById(userId).orElseThrow(() -> new FoundException(String.format("Пользователь c id=%d не найден.", userId)));
        if (userRepository.findUserByEmail(userDto.getEmail()).isPresent() &&
                !updateUser.getEmail().equals(userDto.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        if (userDto.getEmail() != null) {
            updateUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            updateUser.setName(userDto.getName());
        }
        User user = userRepository.save(updateUser);
        return UserMapper.userToDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

}
