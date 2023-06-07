package ru.practicum.shareit.booking;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.Constants.DATE_TIME_FORMATTER;

@WebMvcTest({BookingController.class})
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private BookingOutDto bookingDto;

    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        BookingOutDto.Item item = new BookingOutDto.Item(1L, "молоток");

        BookingOutDto.Booker booker = new BookingOutDto.Booker(2L, "booker");

        bookingDto = new BookingOutDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.parse(now.plusDays(1).format(DATE_TIME_FORMATTER)));
        bookingDto.setEnd(LocalDateTime.parse(now.plusDays(2).format(DATE_TIME_FORMATTER)));
        bookingDto.setItem(item);
        bookingDto.setBooker(booker);
        bookingDto.setStatus(Status.APPROVED);

    }

    @Test
    void createBookingTest() throws Exception {
        BookingDto bookingNewDto = new BookingDto();
        bookingNewDto.setItemId(1L);
        bookingNewDto.setStart(now.plusDays(1));
        bookingNewDto.setEnd(now.plusDays(2));

        when(bookingService.createBooking(anyLong(), any(BookingDto.class)))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingNewDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class));
    }

    @Test
    void updateBookingApproveStatusTest() throws Exception {
        when(bookingService.updateBookingApproveStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookingApproveStatusNoParamTest() throws Exception {
        when(bookingService.updateBookingApproveStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void findBookingByIdTest() throws Exception {
        when(bookingService.findBookingById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDto)));
    }

    @Test
    void findAllBookingTest() throws Exception {
        List<BookingOutDto> bookingDtoList = List.of(bookingDto);
        when(bookingService.findAllBookingByUserAndState(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookingDtoList);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDto))));
    }

    @Test
    void getAll_WrongState_ReturnBadRequest() throws Exception {
        when(bookingService.findAllBookingByUserAndState(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(AccessException.class);
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNSUPPORTED")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void geOwnerAll_Normal() throws Exception {
        List<BookingOutDto> bookingDtoList = List.of(bookingDto);
        when(bookingService.findAllBookingByOwnerAndState(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookingDtoList);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDto))));
    }

    @Test
    void geOwnerAll_WrongState_ReturnBadRequest() throws Exception {
        when(bookingService.findAllBookingByOwnerAndState(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(AccessException.class);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "UNSUPPORTED")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
