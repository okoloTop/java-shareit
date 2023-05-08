package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> findAllUsers() {
        log.info("GET /users - получение списка вcех пользователей.");
        return userService.findAll();
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users - создание нового пользователя.");
        return userService.createUser(userDto);
    }

    @GetMapping("{userId}")
    public UserDto findUserById(@NotNull @PathVariable Long userId) {
        log.info("GET /users - получение пользователя по id.");
        return userService.findUserById(userId);
    }

    @PatchMapping("{userId}")
    public UserDto updateUser(@NotNull @PathVariable Long userId,
                              @Valid @RequestBody UserDto userDto) {
        log.info("PATCH /users - обновление пользователя.");
        return userService.updateUser(userId, userDto);
    }

    @DeleteMapping("{userId}")
    public void deleteUser(@NotNull @PathVariable Long userId) {
        log.info("DELETE /users - удаление пользователя.");
        userService.deleteUserById(userId);
    }
}

