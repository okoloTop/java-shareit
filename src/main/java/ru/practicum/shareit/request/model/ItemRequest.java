package ru.practicum.shareit.request.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private Long requestorId;
    private LocalDateTime created;
}
