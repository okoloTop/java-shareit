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
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findAll() {
        return userListToDto(userRepository.findAll());
    }

    private List<UserDto> userListToDto(List<User> userList) {
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            userDtoList.add(userMapper.userToDto(user));
        }
        return userDtoList;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new InvalidParameterException("Пустой адрес электронной почты");
        }
        User user = userRepository.save(userMapper.dtoToUser(userDto));
        return userMapper.userToDto(user);
    }

    @Override
    public UserDto findUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new FoundException("Такого пользователя нет в базе");
        }
        return userMapper.userToDto(user.get());
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        if (userRepository.findUserById(userId).isEmpty()) {
            throw new FoundException("Такого пользователя нет в базе");
        }
        if (userRepository.findUserByEmail(userDto.getEmail()).isPresent() &&
                !userRepository.findUserById(userId).get().getEmail().equals(userDto.getEmail())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
        User updateUser = userRepository.findUserById(userId).get();
        if (userDto.getEmail() != null) {
            updateUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            updateUser.setName(userDto.getName());
        }
        User user = userRepository.save(updateUser);
        return userMapper.userToDto(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User findFullUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new FoundException("Пользователь не найден"));
    }

}
