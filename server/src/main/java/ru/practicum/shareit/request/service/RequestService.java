package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestInDto;

import java.util.List;

public interface RequestService {
    RequestDto createItemRequest(Long userId, RequestInDto requestInDto);

    List<RequestDto> findAllRequestByUserId(Long userId, Integer from, Integer size);

    RequestDto getRequestById(Long userId, Long requestId);

    List<RequestDto> getPageableRequestByUserId(Long userId, Integer from, Integer size);
}
