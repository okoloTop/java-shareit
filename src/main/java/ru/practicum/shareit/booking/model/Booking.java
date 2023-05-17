package ru.practicum.shareit.booking.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long itemId;
    private Long booker;
    private Status status;
}
