package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl service;

    LocalDateTime created =
            LocalDateTime.of(2023, 5, 26, 10, 10, 0);

    RequestInDto requestDto;
    Item item;
    User requestor;
    ItemRequest savedItemRequest;
    PageRequest pageRequest = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        requestDto = new RequestInDto("нужен молоток");

        item = Item.builder()
                .id(1L)
                .name("молоток").description("крепкий молоток").available(true)
                .owner(requestor)
                .build();

        requestor = User.builder()
                .id(1L)
                .name("user")
                .email("user@example1")
                .build();

        savedItemRequest = new ItemRequest(1L, "нужен молоток", requestor, created, null);
    }

    @Test
    void createItemRequestTest() {
        Mockito.when(userRepository.findUserById(1L)).thenReturn(Optional.of(requestor));
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedItemRequest);

        RequestDto mustBe = RequestMapper.requestToDto(savedItemRequest);

        RequestDto saved = service.createItemRequest(1L, requestDto);

        Assertions.assertThat(saved)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(mustBe);

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(itemRequestRepository, Mockito.times(1)).save(any(ItemRequest.class));
        Mockito.verifyNoMoreInteractions(userRepository, itemRequestRepository);
    }

    @Test
    void createItemRequestWrongUserTest() {
        String message = String.format("Пользователь не найден");

        Mockito.when(userRepository.findUserById(99L)).thenThrow(new FoundException(message));

        Throwable throwable = Assertions.catchException(() -> service.createItemRequest(99L, requestDto));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage(message);
    }

    @Test
    void findAllByRequestorIdTest() {
        Mockito.when(userRepository.findUserById(1L)).thenReturn(Optional.ofNullable(requestor));
        Mockito.when(itemRequestRepository.findAllByRequestorId(pageRequest,1L)).thenReturn(List.of(savedItemRequest));

        List<RequestDto> returned = service.findAllRequestByUserId(1L, 0, 20);

        Assertions.assertThat(returned)
                .isNotNull()
                .hasSize(1);
        Assertions.assertThat(returned.get(0).getDescription()).isEqualTo(requestDto.getDescription());

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findAllByRequestorId(pageRequest, 1L);
        Mockito.verifyNoMoreInteractions(userRepository, itemRequestRepository);
    }

    @Test
    void findAllByRequestorWrongUserTest() {
        String message = String.format("Пользователь не найден");

        Mockito.when(userRepository.findUserById(99L)).thenThrow(new FoundException(message));

        Throwable throwable = Assertions.catchException(() -> service.findAllRequestByUserId(99L, 0, 20));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage(message);

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(99L);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findItemRequestsTest() {
        Mockito.when(userRepository.findUserById(1L)).thenReturn(Optional.of(requestor));
        Mockito.when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(pageRequest, 1L))
                .thenReturn(List.of(savedItemRequest));

        List<RequestDto> returned = service.getPageableRequestById(1L, 0, 20);

        Assertions.assertThat(returned)
                .isNotNull()
                .hasSize(1);
    }

    @Test
    void getItemRequestTest() {
        Mockito.when(userRepository.findUserById(1L)).thenReturn(Optional.of(requestor));
        Mockito.when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(savedItemRequest));

        RequestDto mustBe = RequestMapper.requestToDto(savedItemRequest);

        RequestDto returned = service.getRequestById(1L, 1L);

        Assertions.assertThat(returned)
                .isNotNull()
                .usingRecursiveComparison().isEqualTo(mustBe);

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(1L);
        Mockito.verifyNoMoreInteractions(userRepository, itemRequestRepository);
    }

    @Test
    void getItemRequestWrongUserIdTest() {
        String message = String.format("Пользователь не найден");

        when(userRepository.findUserById(99L)).thenReturn(Optional.empty());

        Throwable throwable = Assertions.catchException(() -> service.getRequestById(99L, 1L));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage(message);

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(99L);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getItemRequestWrongItemRequestIdTest() {
        String message = String.format("Запрос c id=%d не найден.", 99L);

        Mockito.when(userRepository.findUserById(1L)).thenReturn(Optional.of(requestor));
        Mockito.when(itemRequestRepository.findById(99L))
                .thenThrow(new FoundException(message));

        Throwable throwable = Assertions.catchException(() -> service.getRequestById(1L, 99L));

        Assertions.assertThat(throwable)
                .isInstanceOf(FoundException.class)
                .hasMessage(message);

        Mockito.verify(userRepository, Mockito.times(1)).findUserById(1L);
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findById(99L);
        Mockito.verifyNoMoreInteractions(userRepository, itemRequestRepository);
    }
}
