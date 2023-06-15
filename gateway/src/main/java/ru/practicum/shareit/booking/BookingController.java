package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exeption.IllegalArgumentException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        checkCorrectState(stateParam);
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getAllBookings(userId, stateParam, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {
        checkCorrectDateTime(requestDto);
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.create(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> updateBookingApproveStatus(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                             @NotNull @PathVariable Long bookingId,
                                                             @RequestParam @NotNull Boolean approved) {
        log.info("PATCH /bookings - обновление бронирования вещи");
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllBookingByOwnerAndState(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                                                @RequestParam(name = "state", required = false, defaultValue = "ALL")
                                                                String stateParam,
                                                                @PositiveOrZero
                                                                @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                                @Positive
                                                                @RequestParam(name = "size", defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        checkCorrectState(stateParam);
        log.info("GET /bookings / owner - получение бронирований пользователя по параметру");
        return bookingClient.getAllBookingsForOwner(ownerId, stateParam, from, size);
    }

    private void checkCorrectDateTime(BookItemRequestDto bookingInDto) {
        if (bookingInDto.getStart() == null || bookingInDto.getEnd() == null) {
            throw new IllegalArgumentException("Укажите корректные даты бронирования вещи");
        }
        if (bookingInDto.getStart().equals(bookingInDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала бронирования и окончания не должна совпадать");
        }
        if (bookingInDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата начала бронирования должна быть в будущем");
        }
        if (bookingInDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата окончания бронирования должна быть в будущем");
        }
        if (bookingInDto.getEnd().isBefore(bookingInDto.getStart())) {
            throw new IllegalArgumentException("Некорректные даты бронирования");
        }
    }

    private void checkCorrectState(String state) {
        if (!Objects.equals(state, "ALL")) {
            List<String> stateList = List.of("CURRENT", "PAST", "FUTURE", "REJECTED", "WAITING");
            if (!stateList.contains(state)) {
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
    }
}
