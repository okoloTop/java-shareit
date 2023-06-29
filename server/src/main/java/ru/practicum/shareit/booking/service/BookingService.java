package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto createBooking(Long userId, BookingDto bookingInDto);

    BookingOutDto updateBookingApproveStatus(Long userId, Long bookingId, Boolean approved);

    BookingOutDto findBookingById(Long userId, Long bookingId);

    List<BookingOutDto> findAllBookingByUserAndState(Long userId, String state, Integer from, Integer size);

    List<BookingOutDto> findAllBookingByOwnerAndState(Long ownerId, String state, Integer from, Integer size);

}
