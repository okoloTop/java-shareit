package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "user", "user@example.com");

        user = User.builder()
                .id(1L)
                .name("user")
                .email("user@example.com")
                .build();
    }

    @Test
    void saveUserTest() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto added = userService.createUser(userDto);

        Assertions.assertThat(added)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveUserEmailExistsTest() {
        when(userRepository.save(any(User.class)))
                .thenThrow(AccessException.class);

        Throwable thrown = Assertions.catchException(() -> userService.createUser(userDto));

        Assertions.assertThat(thrown)
                .isInstanceOf(AccessException.class);

        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserTest() {
        final UserDto updatedData = new UserDto(1L, "updatedName", "updatedName@example.com");
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);


        UserDto updatedUser = userService.updateUser(1L, updatedData);

        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("updatedName@example.com");
        Assertions.assertThat(updatedUser.getName()).isEqualTo("updatedName");

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    void updateUserNameTest() {
        final UserDto userDto = new UserDto(1L, "updatedName", "user@example.com");

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto updatedUser = userService.updateUser(userDto.getId(), userDto);

        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("user@example.com");
        Assertions.assertThat(updatedUser.getName()).isEqualTo("updatedName");

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    void updateUserNameEmpty() {
        final UserDto userDto = new UserDto(1L, null, "updatedName@example.com");

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto updatedUser = userService.updateUser(userDto.getId(), userDto);

        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("updatedName@example.com");
        Assertions.assertThat(updatedUser.getName()).isEqualTo("user");

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    void updateUserEmailDuplicateTest() {
        final UserDto userDto = new UserDto(1L, "user", "user@example.com");

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto returnedUser = userService.updateUser(userDto.getId(), userDto);

        Assertions.assertThat(returnedUser.getEmail()).isEqualTo("user@example.com");
        Assertions.assertThat(returnedUser.getName()).isEqualTo("user");

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    void findUserByIdTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto actual = userService.findUserById(1L);

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(user);
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUserByIdWrongIdTest() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Throwable thrown = Assertions.catchException(() -> userService.findUserById(999L));

        Assertions.assertThat(thrown)
                .isInstanceOf(FoundException.class)
                .hasMessage(String.format("Такого пользователя нет в базе"));

        Mockito.verify(userRepository, Mockito.times(1)).findById(999L);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUserTest() {
        Mockito.doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUserById(anyLong());

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(anyLong());
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAllUsersTest() {
        User user2 = User.builder()
                .id(2L)
                .name("user2")
                .email("user2@example.com")
                .build();
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserDto> users = userService.findAll();

        Assertions.assertThat(users)
                .isNotNull()
                .hasSize(2)
                .contains(UserMapper.userToDto(user),
                        UserMapper.userToDto(user2));
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(userRepository);
    }
}
