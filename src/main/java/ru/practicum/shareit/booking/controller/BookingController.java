package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto createBooking(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                       @Valid @RequestBody BookingDto bookingDto) {
        log.info("POST /bookings - создание бронирования вещи");
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public BookingOutDto updateBookingApproveStatus(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                    @NotNull @PathVariable Long bookingId,
                                                    @RequestParam @NotNull Boolean approved) {
        log.info("PATCH /bookings - обновление бронирования вещи");
        return bookingService.updateBookingApproveStatus(userId, bookingId, approved);
    }

    @GetMapping("{bookingId}")
    public BookingOutDto findBookingById(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                         @NotNull @PathVariable Long bookingId) {
        log.info("GET /bookings/{bookingId} - получение бронирования по идентификатору");
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> findAllBookingByUserAndState(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                            @RequestParam(name = "state", required = false, defaultValue = "ALL")
                                                            String state,
                                                            @PositiveOrZero
                                                            @RequestParam(defaultValue = "0") Integer from,
                                                            @Positive
                                                            @RequestParam(defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /bookings - получение бронирований пользователя по параметру");
        return bookingService.findAllBookingByUserAndState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findAllBookingByOwnerAndState(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                                             @RequestParam(name = "state", required = false, defaultValue = "ALL")
                                                             String state,
                                                             @PositiveOrZero
                                                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                             @Positive
                                                             @RequestParam(name = "size", defaultValue = Constants.PAGE_SIZE_STRING) Integer size) {
        log.info("GET /bookings / owner - получение бронирований пользователя по параметру");
        return bookingService.findAllBookingByOwnerAndState(ownerId, state, from, size);
    }
}
