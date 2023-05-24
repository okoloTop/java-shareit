package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingOutDto createBooking(Long userId, BookingDto bookingInDto);

    BookingOutDto updateBookingApproveStatus(Long userId, Long bookingId, String bookingStatus);

    BookingOutDto findBookingById(Long userId, Long bookingId);

    List<BookingOutDto> findAllBookingByUserAndState(Long userId, String state);

    List<BookingOutDto> findAllBookingByOwnerAndState(Long ownerId, String state);

    List<BookingOutDto> findAllBookingByOwnerIdAndItemId(Long itemId);

    List<Booking> findAllBookingByUserIdAndItemId(Long userId, Long itemId, LocalDateTime dateTime);
}
