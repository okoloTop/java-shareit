package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Validated
@Slf4j
public class UserController {

    private UserClient client;


    @GetMapping
    public ResponseEntity<Object> findAllUsers() {
        log.info("GET /users - получение списка вcех пользователей.");
        return client.findAll();
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserDto userDto) {
        log.info("POST /users - создание нового пользователя.");
        return client.registerUser(userDto);
    }

    @GetMapping("{userId}")
    public ResponseEntity<Object> findUserById(@NotNull @PathVariable Long userId) {
        log.info("GET /users - получение пользователя по id.");
        return client.getUser(userId);
    }

    @PatchMapping("{userId}")
    public ResponseEntity<Object> updateUser(@NotNull @PathVariable Long userId,
                                             @RequestBody CreateUserDto userDto) {
        log.info("PATCH /users - обновление пользователя.");
        return client.updateUser(userId, userDto);
    }

    @DeleteMapping("{userId}")
    public void deleteUser(@NotNull @PathVariable Long userId) {
        log.info("DELETE /users - удаление пользователя.");
        client.deleteUser(userId);
    }
}
