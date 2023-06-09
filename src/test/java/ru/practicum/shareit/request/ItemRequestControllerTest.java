package ru.practicum.shareit.request;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.FoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;
import ru.practicum.shareit.request.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.Constants.DATE_TIME_FORMATTER;

@WebMvcTest({ItemRequestController.class})
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    RequestService itemRequestService;

    private final LocalDateTime now = LocalDateTime
            .of(2023, 6, 05, 12, 0, 0, 0);

    RequestInDto requestInDto;
    RequestDto requestDto;
    RequestDto requestDto2;

    @BeforeEach
    void setUp() {
        requestInDto = new RequestInDto("нужен молоток");

        requestDto = new RequestDto(1L, "нужен молоток", 1L, now, new ArrayList<>());

        requestDto2 = new RequestDto(2L, "нужен стол", 1L, now, new ArrayList<>());
    }

    @Test
    void createItemRequestTest() throws Exception {
        when(itemRequestService.createItemRequest(anyLong(), any(RequestInDto.class)))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().format(DATE_TIME_FORMATTER)), LocalDateTime.class));
    }

    @Test
    void createItemRequestWrongUserIdTest() throws Exception {
        when(itemRequestService.createItemRequest(anyLong(), any(RequestInDto.class)))
                .thenThrow(FoundException.class);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 99L)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllByRequestorTest() throws Exception {

        when(itemRequestService.findAllRequestByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto, requestDto2));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(requestDto, requestDto2))));
    }

    @Test
    void findAllByRequestorWrongUserIdTest() throws Exception {
        when(itemRequestService.findAllRequestByUserId(anyLong(), anyInt(), anyInt()))
                .thenThrow(FoundException.class);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 99L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllTest() throws Exception {
        when(itemRequestService.getPageableRequestByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto, requestDto2));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(requestDto, requestDto2))));
    }

    @Test
    void findAllWrongUserIdTest() throws Exception {
        when(itemRequestService.getPageableRequestByUserId(anyLong(), anyInt(), anyInt()))
                .thenThrow(FoundException.class);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 99L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemRequestTest() throws Exception {
        when(itemRequestService.getRequestById(1L, 1L))
                .thenReturn(requestDto);

        mockMvc.perform(get("/requests/{id}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.description", is("нужен молоток")));
    }

    @Test
    void getItemRequestWrongUserIdTest() throws Exception {
        when(itemRequestService.getRequestById(99L, 1L))
                .thenThrow(FoundException.class);

        mockMvc.perform(get("/requests/{id}", 1L)
                        .header("X-Sharer-User-Id", 99L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemRequestWrongItemRequestIdTest() throws Exception {
        when(itemRequestService.getRequestById(1L, 99L))
                .thenThrow(FoundException.class);

        mockMvc.perform(get("/requests/{id}", 99L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
