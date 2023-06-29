package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;

@UtilityClass
public class BookingMapper {
    public static Booking dtoToBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        return booking;
    }

    public static BookingOutDto toBookingDto(Booking booking) {
        return new BookingOutDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new BookingOutDto.Item(
                        booking.getItem().getId(),
                        booking.getItem().getName()
                ),
                new BookingOutDto.Booker(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()
                ));
    }
}