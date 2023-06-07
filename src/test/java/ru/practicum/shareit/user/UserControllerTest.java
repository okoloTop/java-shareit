package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    UserDto user;

    @BeforeEach
    void setUp() {
        user = new UserDto(1L, "user", "user@example.com");
    }

    @Test
    void createUserTest() throws Exception {
        Mockito.when(userService.createUser(user)).thenReturn(user);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("user"));
    }

    @Test
    void createUserWrongEmailTest() throws Exception {
        user.setEmail("example.com");

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void updateUserTest() throws Exception {
        user.setEmail("user@example.com");
        user.setName("updatedUser");

        UserDto userUpdated = new UserDto(1L, "user2", "user@example.com");

        Mockito.when(userService.updateUser(user.getId(), user))
                .thenReturn(userUpdated);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("user2"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void FindByIdTest() throws Exception {
        Mockito.when(userService.findUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1L)
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void findAllUserTest() throws Exception {
        UserDto user1 = new UserDto(1L, "user", "user@example.com");
        UserDto user2 = new UserDto(1L, "user2", "user2@example.com");

        List<UserDto> userList = List.of(user1, user2);

        Mockito.when(userService.findAll()).thenReturn(userList);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(List.of(user1, user2))));
    }
}
